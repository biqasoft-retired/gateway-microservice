/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.repositories;

import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.gateway.externalservice.ExternalServiceTokenRepository;
import com.biqasoft.gateway.storage.dto.GoogleOauthResponseToken;
import com.biqasoft.microservice.communicator.http.HttpClientsHelpers;
import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.storage.StorageFileRepository;
import com.biqasoft.storage.entity.StorageFile;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import org.javers.common.collections.Sets;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Google Drive
 * Storage Integration
 * <p>
 * <p>
 * {@code https://security.google.com/settings/security/permissions}
 * {@code https://developers.google.com/drive/v2/reference/parents/get}
 * {@code https://developers.google.com/drive/web/folder}
 * {@code https://developers.google.com/drive/v2/reference/files}
 * {@code https://developers.google.com/drive/v2/reference/files/list}
 * {@code https://developers.google.com/drive/web/manage-downloads}
 * {@code https://developers.google.com/apis-explorer/#p/drive/v2/}
 * {@code https://developers.google.com/drive/v2/reference/files/get}
 */
@Service
@ConditionalOnProperty({"google.drive.CLIENT_ID_KEY", "biqa.REQUIRE_ALL"})
public class GoogleDriveStorageRepository implements StorageFileRepository {

    @Value("${biqasoft.httpclient.name}")
    private String biqaHttpClientName;

    @Value("${google.drive.CLIENT_ID_KEY}")
    private String googleDriveClientId;

    @Value("${google.drive.CLIENT_SECRET}")
    private String googleDriveClientSecret;

    @Value("${google.drive.REDIRECT_URI_KEY}")
    private String googleDriveRedirectURI;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private ExternalServiceTokenRepository externalServiceTokenRepository;

    @Autowired
    private DefaultStorageService defaultStorageService;

    @Autowired
    private StorageUserRepository storageUserRepository;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    private final static String GOOGLE_DRIVE_TOKEN_URI = "https://www.googleapis.com/oauth2/v3/token";
    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    static {
        try {
//            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            //            builder.trustCertificates( Security.getProvider() );
            NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
            // TODO: security issue
            builder.doNotValidateCertificate();

            HTTP_TRANSPORT = builder.build();
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }

    @Override
    public void onBeforeUploadMetaInfoFile(StorageFile documentFile) {
        if (documentFile.isFolder()) {
            ExternalServiceToken token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID());
            createFolder(token, documentFile, documentFile.getName(), documentFile.getParentId());
        }

        storageUserRepository.processTempFileForMetaInformation(documentFile);
    }

    @Override
    public String getStorageName() {
        return TOKEN_TYPES.GOOGLE_DRIVE;
    }

    @Override
    public String processListingPath(String path) {
        if (StringUtils.isEmpty(path)) path = "/";
        return path;
    }

    @Override
    public void onAfterUploadFile(File file, StorageFile documentFile) {
        defaultStorageService.deleteStorageFileFromDataBase(documentFile);
    }

    @Override
    public void createFolder(ExternalServiceToken externalServiceToken, StorageFile documentFile, String folderName, String path) {
        ExternalServiceToken token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID());

        Drive drive = getDriveClient(token);

        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
        file.setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE);
        file.setTitle(documentFile.getName());

        List<ParentReference> parentReferences = new ArrayList<>();
        parentReferences.add(new ParentReference().setId(documentFile.getParentId()));
        file.setParents(parentReferences);

        try {
            drive.files().insert(file).execute();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteDocumentFile(StorageFile documentFile) {
        ExternalServiceToken token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID());
        Drive drive = getDriveClient(token);

        try {
            drive.files().delete(documentFile.getId()).execute();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    @Override
    public ByteArrayOutputStream downloadFileWithToken(StorageFile documentFile, ExternalServiceToken externalServiceToken) {
        Drive drive = getDriveClient(externalServiceToken);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            drive.files().get(documentFile.getId()).executeMediaAndDownloadTo(stream);
            return stream;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<StorageFile> getListing(ExternalServiceToken externalServiceToken, String path) {
        Drive service = getDriveClient(externalServiceToken);

        // Print the names and IDs for up to 10 files.
        FileList result;

        try {
            result = service.files().list().setQ("'" + path + "' in parents and trashed=false")
                    .setMaxResults(Integer.MAX_VALUE)
//                    .setMaxResults(10)
                    .execute();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        List<com.google.api.services.drive.model.File> files = result.getItems();
        return transferFilesFromGoogleAPItoInternal(files, externalServiceToken);
    }

    @Override
    public StorageFile uploadFile(File file, StorageFile documentFile, UserAccount userAccount, Domain domain) {
        ExternalServiceToken token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID());
        Drive drive = getDriveClient(token);

        com.google.api.services.drive.model.File fileGoogle = new com.google.api.services.drive.model.File();
        fileGoogle.setOriginalFilename(documentFile.getName());
        fileGoogle.setTitle(documentFile.getName());

        FileContent mediaContent = new FileContent(defaultStorageService.getContentTypeFromFileName(documentFile.getName()), file);

        List<ParentReference> references = new ArrayList<>();
        ParentReference parentReference = new ParentReference();
        parentReference.setId(documentFile.getParentId());
        references.add(parentReference);
        fileGoogle.setParents(references);

        try {
            com.google.api.services.drive.model.File uploadedFile = drive.files().insert(fileGoogle, mediaContent).execute();

            List<com.google.api.services.drive.model.File> documentFiles = new ArrayList<>();
            documentFiles.add(uploadedFile);

            return transferFilesFromGoogleAPItoInternal(documentFiles, token).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<? extends StorageFile> getMetaInfo(String dir, ExternalServiceToken externalServiceToken) {
        return null;
    }

    public ExternalServiceToken obtainCodeToToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();

        form.add("code", code);
        form.add("client_id", googleDriveClientId);
        form.add("client_secret", googleDriveClientSecret);
        form.add("redirect_uri", googleDriveRedirectURI);
        form.add("grant_type", "authorization_code");

        RestTemplate template = HttpClientsHelpers.getRestTemplate();

        GoogleOauthResponseToken re = template.postForObject(GOOGLE_DRIVE_TOKEN_URI, form, GoogleOauthResponseToken.class);

        ExternalServiceToken externalServiceToken = new ExternalServiceToken();
        externalServiceToken.setType(TOKEN_TYPES.GOOGLE_DRIVE);
        externalServiceToken.setToken(re.getAccess_token());
        externalServiceToken.setRefreshToken(re.getRefresh_token());

        DateTime dateTime = new DateTime(new Date());
        dateTime.plusSeconds(re.getExpires_in());
        // google token have expires
        externalServiceToken.setExpired(dateTime.toDate());

        Drive drive = getDriveClient(externalServiceToken, true);

        try {
            externalServiceToken.setName(drive.about().get().execute().getName());
            externalServiceToken.setLogin(drive.about().get().execute().getUser().getEmailAddress());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        ExternalServiceToken existingTokenWithSameLogin = externalServiceTokenRepository.findExternalServiceTokenByLoginAndTypeIgnoreExpired(externalServiceToken.getLogin(), TOKEN_TYPES.GOOGLE_DRIVE);

        // if we DON'T already have user with the same login
        // add new token
        if (existingTokenWithSameLogin == null) {
            externalServiceTokenRepository.addExternalServiceToken(externalServiceToken);
        } else {
            // if we have token with current user
            // but we have new refresh token
            // we would update our token info
            if (re.getRefresh_token() != null && !re.getRefresh_token().equals("")) {
                externalServiceToken.setId(existingTokenWithSameLogin.getId());
                externalServiceToken.setDomain(existingTokenWithSameLogin.getDomain());
                externalServiceToken.setCreatedInfo(new CreatedInfo(new Date(), currentUser.getCurrentUser().getId()));
                externalServiceTokenRepository.updateExternalServiceToken(externalServiceToken);
            } else {
                // we already have this user, but have not
                // refresh token - bad situation
                // which should not be...
            }
        }
        // otherwise
        // if we have not refresh token from google
        // and we already have user with this login
        // we would not do anything

        return externalServiceToken;
    }

    /**
     * See more at
     * <p>
     * {@code https://developers.google.com/identity/protocols/OAuth2WebServer}
     * {@code https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=ya29.wwGA_WB3GbyB4P6ExslSRMh5Ae6ObXyWmVBPK_pLSAj7NpXFg9h3OFDPhcRVY3_drhYy}
     */
    private ExternalServiceToken refreshAccessCodeFromRefreshToken(ExternalServiceToken externalServiceToken) {
        // this is only for google token
        if (!externalServiceToken.getType().equals(TOKEN_TYPES.GOOGLE_DRIVE)) return null;
        // this method is only update, not create new
        if (externalServiceTokenRepository.findExternalServiceTokenByIdIgnoreExpired(externalServiceToken.getId()) == null)
            return null;

        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();

        form.add("refresh_token", externalServiceToken.getRefreshToken());
        form.add("client_id", googleDriveClientId);
        form.add("client_secret", googleDriveClientSecret);
        form.add("grant_type", "refresh_token");

        RestTemplate template = HttpClientsHelpers.getRestTemplate();
        GoogleOauthResponseToken re = template.postForObject(GOOGLE_DRIVE_TOKEN_URI, form, GoogleOauthResponseToken.class);

        externalServiceToken.setToken(re.getAccess_token());

        DateTime dateTime = new DateTime(new Date());
        dateTime = dateTime.plusSeconds(re.getExpires_in());
        externalServiceToken.setExpired(dateTime.toDate());

        externalServiceTokenRepository.updateExternalServiceToken(externalServiceToken);

        return externalServiceToken;
    }

    private static Set<String> onlyExport;

    static {
        onlyExport = Sets.asSet("application/vnd.google-apps.spreadsheet", "application/vnd.google-apps.spreadsheet", "application/vnd.google-apps.audio",
                "application/vnd.google-apps.document", "application/vnd.google-apps.drawing", "application/vnd.google-apps.file", "application/vnd.google-apps.form",
                "application/vnd.google-apps.fusiontable", "application/vnd.google-apps.photo", "application/vnd.google-apps.presentation", "application/vnd.google-apps.script",
                "application/vnd.google-apps.sites", "application/vnd.google-apps.unknown", "application/vnd.google-apps.video");
    }

    private List<StorageFile> transferFilesFromGoogleAPItoInternal(List<com.google.api.services.drive.model.File> googleFiles, ExternalServiceToken externalServiceToken) {
        List<StorageFile> fileList = new ArrayList<>();

        for (com.google.api.services.drive.model.File file : googleFiles) {
            StorageFile documentFile = new StorageFile();
            documentFile.setId(file.getId());
            documentFile.setName(file.getTitle());

            if (file.getFileSize() != null) {
                documentFile.setFileSize(file.getFileSize());
            }

            String mimeType = file.getMimeType();

            documentFile.setMimeType(mimeType);

            // this is google mime type for folders
            if (mimeType != null && mimeType.equals(GOOGLE_DRIVE_FOLDER_MIME_TYPE)) {
                documentFile.setFullName(file.getId());
                documentFile.setFolder(true);
            } else {
                documentFile.setFile(true);
                documentFile.setExportLinks(file.getExportLinks());

                if (onlyExport.contains(mimeType)) {
                    documentFile.setOnlyExport(true);
                }
            }

            documentFile.setUploadStoreType(TOKEN_TYPES.GOOGLE_DRIVE);
            documentFile.setUploadStoreID(externalServiceToken.getId());

            documentFile.setAvatarUrl(file.getThumbnailLink());
            documentFile.setVersion(file.getVersion().intValue());

            Date date = file.getModifiedDate() != null ? (new Date(file.getModifiedDate().getValue())) : null;

            //String modifier = file.getOwnerNames()!= null  ? ( file.getOwnerNames().size() > 0 ? file.getOwnerNames().get(0)  : ""  ) : "";
            String modifier = null;
            // don't add owner name
            // because frontend will not be able
            // to resolve useraccount with this id
            documentFile.setCreatedInfo(new CreatedInfo(date, modifier));
            fileList.add(documentFile);
        }

        return fileList;
    }

    // auto update google access token
    private ExternalServiceToken refreshTokenIfNeed(ExternalServiceToken token) {
        if (token != null && token.getType().equals(TOKEN_TYPES.GOOGLE_DRIVE)) {
            DateTime now = new DateTime(new Date());
            DateTime tokenExpiresTime = new DateTime(token.getExpired());

            if (now.isAfter(tokenExpiresTime)) {
                token = refreshAccessCodeFromRefreshToken(token);
            }
        }
        return token;
    }

    private Drive getDriveClient(ExternalServiceToken token) {
        return getDriveClient(token, false);
    }

    /**
     * Create Google Drive client from token
     *
     * @param token
     * @param ignoreExpired do not try to update token if it is expired used when we have not yet created token in database
     * @return
     */
    private Drive getDriveClient(ExternalServiceToken token, Boolean ignoreExpired) {

        if (ignoreExpired != null && !ignoreExpired) {
            token = refreshTokenIfNeed(token);
        }

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
        credential.setAccessToken(token.getToken());

        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(biqaHttpClientName)
                .build();
    }

}
