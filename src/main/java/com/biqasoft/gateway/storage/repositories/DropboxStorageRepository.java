/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.repositories;

import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.users.domain.useraccount.UserAccount;
import com.biqasoft.gateway.externalservice.ExternalServiceTokenRepository;
import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.storage.StorageFileRepository;
import com.dropbox.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnProperty({"dropbox.app.key", "biqa.REQUIRE_ALL"})
public class DropboxStorageRepository implements StorageFileRepository {

    private final ExternalServiceTokenRepository externalServiceTokenRepository;
    private StorageUserRepository storageUserRepository;
    private final DefaultStorageService defaultStorageService;

    private final String dropboxAppRedirect;
    private final String dropboxCSRF;

    private final DbxAppInfo appInfo;
    private final DbxRequestConfig config;

    public DbxWebAuth getDbxWebAuth(){
        return new DbxWebAuth(config, appInfo, dropboxAppRedirect, new DbxMoc(dropboxCSRF));
    }

    public String getDropboxCSRF() {
        return dropboxCSRF;
    }

    public DbxRequestConfig getConfig() {
        return config;
    }

    @Override
    public void onBeforeUploadMetaInfoFile(StorageFile documentFile) {
        if (documentFile.isFolder()) {
            ExternalServiceToken token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID());
            createFolder(token, documentFile, documentFile.getName(), documentFile.getParentId());
        }
        storageUserRepository.processTempFileForMetaInformation(documentFile);
    }

    @Autowired
    public void setStorageUserRepository(StorageUserRepository storageUserRepository) {
        this.storageUserRepository = storageUserRepository;
    }

    @Override
    public String getStorageName() {
        return TOKEN_TYPES.DROPBOX;
    }

    @Autowired
    public DropboxStorageRepository(ExternalServiceTokenRepository externalServiceTokenRepository,
                                    DefaultStorageService defaultStorageService,
                                    @Value("${dropbox.app.redirect}") String dropboxAppRedirect,
                                    @Value("${dropbox.app.csrf}") String dropboxCSRF,
                                    @Value("${dropbox.app.key}") String dropBoxAppKey,
                                    @Value("${dropbox.app.secret}") String dropBoxAppSecret,
                                    @Value("${biqasoft.httpclient.name}") String biqaHttpClientName) {
        this.externalServiceTokenRepository = externalServiceTokenRepository;
        this.defaultStorageService = defaultStorageService;

        this.appInfo = new DbxAppInfo(dropBoxAppKey, dropBoxAppSecret);
        this.config = new DbxRequestConfig(biqaHttpClientName, Locale.getDefault().toString());
        this.dropboxAppRedirect = dropboxAppRedirect;
        this.dropboxCSRF = dropboxCSRF;
    }

    @Override
    public String processListingPath(String path) {
        if (path == null || path.equals("")) path = "root";
        return path;
    }

    @Override
    public ByteArrayOutputStream downloadFileWithToken(StorageFile documentFile, ExternalServiceToken externalServiceToken) {
        DbxClient client = new DbxClient(config, externalServiceToken.getToken());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            client.getFile(documentFile.getFullName(), null, outputStream);
        } catch (IOException | DbxException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
//            outputStream.close();
        }
        return outputStream;
    }

    @Override
    public void createFolder(ExternalServiceToken externalServiceToken, StorageFile documentFile, String folderName, String path) {
        DbxClient client = new DbxClient(config, externalServiceToken.getToken());

        try {
            client.createFolder(path + "/" + folderName);
        } catch (DbxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<StorageFile> getListing(ExternalServiceToken externalServiceToken, String path) {
        return (List<StorageFile>) getMetaInfo(path, externalServiceToken);
    }

    @Override
    public void onAfterUploadFile(File file, StorageFile documentFile) {
        defaultStorageService.deleteStorageFileFromDataBase(documentFile);
    }

    private void processFileName(StorageFile documentFile) {
        String pathWithName = null;

        if (documentFile.getFullName() == null) {
            pathWithName = "/" + documentFile.getName();
        } else {
            pathWithName = documentFile.getFullName() + "/" + documentFile.getName();
        }
        documentFile.setFullName(pathWithName);
    }

    @Override
    public StorageFile uploadFile(File inputFile, StorageFile documentFile, UserAccount userAccount, Domain domain) {

        processFileName(documentFile);

        String token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID()).getToken();
        DbxClient client = new DbxClient(config, token);
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(inputFile);

            DbxEntry.File uploadedFile = client.uploadFile(documentFile.getFullName(),
                    DbxWriteMode.add(), inputFile.length(), inputStream);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (DbxException e) {
            throw new RuntimeException(e.getMessage());
        }

        return documentFile;
    }

    @Override
    public boolean deleteDocumentFile(StorageFile documentFile) {
        String token = externalServiceTokenRepository.findExternalServiceTokenById(documentFile.getUploadStoreID()).getToken();
        DbxClient client = new DbxClient(config, token);
        try {
            client.delete(documentFile.getFullName());
        } catch (DbxException e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    @Override
    public List<? extends StorageFile> getMetaInfo(String path, ExternalServiceToken externalServiceToken) {
        List<StorageFile> fileList = new ArrayList<>();
        DbxClient client = new DbxClient(config, externalServiceToken.getToken());
        DbxEntry.WithChildren listing;

        try {
            listing = client.getMetadataWithChildren(path);
        } catch (DbxException e) {
            throw new RuntimeException(e.getMessage());
        }

        // list all files and cast to internal storage
        if (listing != null) {
            for (DbxEntry child : listing.children) {
                StorageFile documentFile = new StorageFile();
                documentFile.setName(child.name);
                documentFile.setFullName(child.path);

                // this is file
                if (child instanceof DbxEntry.File) {
                    DbxEntry.File file = (DbxEntry.File) child;
                    documentFile.setFileSize(file.numBytes);
                    documentFile.setCreatedInfo(new CreatedInfo(file.lastModified));
                    documentFile.setFile(true);
                    documentFile.setVersion(file.rev.hashCode());

//                    if ( file.mightHaveThumbnail ){
//                        client.getThumbnail()
//                    }
//
//                    storage.setAvatarUrl(file.mightHaveThumbnail());

                    documentFile.setMimeType(defaultStorageService.getContentTypeFromFileName(documentFile.getName()));
                }

                // this is folder
                if (child instanceof DbxEntry.Folder) {
//                    DbxEntry.Folder file = (DbxEntry.Folder) child;
                    documentFile.setFolder(true);
                }

                documentFile.setUploaded(true);
                documentFile.setUploadStoreType(TOKEN_TYPES.DROPBOX);
                documentFile.setUploadStoreID(externalServiceToken.getId());
                fileList.add(documentFile);
            }
        }
        return fileList;
    }



    /**
     * Created by ya_000 on 7/31/2015.
     */
    private static class DbxMoc implements DbxSessionStore {

        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }


        public DbxMoc(String state) {
            this.state = state;
        }

        @Override
        public String get() {
            return state;
        }

        @Override
        public void set(String value) {

        }

        @Override
        public void clear() {

        }
    }

}
