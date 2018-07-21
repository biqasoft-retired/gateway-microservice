/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.repositories;

import com.biqasoft.entity.annotations.BiqaCheckSecuredModifyObject;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.storage.StorageFileRepository;
import com.biqasoft.storage.s3.S3CompatibleFileRepository;
import com.biqasoft.entity.core.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.biqasoft.entity.constants.TOKEN_TYPES.S3_COMPATIBLE;

@Service
public class StorageUserRepository {

    @Autowired
    private ExternalServiceTokenRepository externalServiceTokenRepository;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DefaultStorageService defaultStorageService;

    @Autowired
    private Map<String, StorageFileRepository> allStorageProvidersMap = new HashMap<>();

    @Autowired
    @TenantDatabase
    private MongoOperations ops;

    @Value("${aws.s3.bucket.user.default}")
    private String defaultBucket;

    @Autowired
    public void setAllStorageProvidersList(List<StorageFileRepository> allStorageProvidersList) {
        allStorageProvidersList.forEach(x -> {
            allStorageProvidersMap.put(x.getStorageName(), x);
        });
    }

    private StorageFileRepository getFileRepositoryByName(StorageFile storageFile) {
        StorageFileRepository repository = null;
        repository = allStorageProvidersMap.get(storageFile.getUploadStoreType());

        if (storageFile.getUploadStoreType().equals(S3_COMPATIBLE)){
            ExternalServiceToken externalServiceToken = externalServiceTokenRepository.findExternalServiceTokenById(storageFile.getUploadStoreID());

            if (externalServiceToken == null){
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider_name");
            }else{
                return new S3CompatibleFileRepository(ops, defaultStorageService, externalServiceToken);
            }
        }

        if (repository == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider_name");
        }
        return repository;
    }

    private StorageFileRepository getFileRepositoryByName(ExternalServiceToken token) {
        StorageFileRepository repository;
        repository = allStorageProvidersMap.get(token.getType());

        if (token.getType().equals(S3_COMPATIBLE)){
            ExternalServiceToken externalServiceToken = externalServiceTokenRepository.findExternalServiceTokenById(token.getId());

            if (externalServiceToken == null){
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider_name");
            }else{
                return new S3CompatibleFileRepository(ops, defaultStorageService, externalServiceToken);
            }
        }

        if (repository == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider_name");
        }
        return repository;
    }

    /**
     * We need to store meta information for uploading files in database
     * for dropbox, google drive etc (to store name of file etc...)
     * after download - we delete it
     *
     * @param documentFile
     */
    public void processTempFileForMetaInformation(StorageFile documentFile) {
        if (documentFile.isFile()) {
            documentFile.setTemp(true);
            documentFile.setArchived(true);
            defaultStorageService.addStorageFile(documentFile);
        }
    }

    /**
     * Process user request in API on uploading file
     * @param documentFile
     * @return
     */
    public StorageFile processMetaInfoForUploadingFile(StorageFile documentFile) {

        DomainSettings domainSettings = currentUser.getCurrentUserDomain();

        // insure that we have some service provider
        if (StringUtils.isEmpty(documentFile.getUploadStoreType()) || StringUtils.isEmpty(domainSettings.getDefaultUploadStoreType()) ) {
            documentFile.setUploadStoreType(TOKEN_TYPES.DEFAULT_STORAGE);

        // set default bucket for s3 compatible
        }else if (documentFile.getUploadStoreType().equals(S3_COMPATIBLE)){
            if (StringUtils.isEmpty(documentFile.getBucket())){
                documentFile.setBucket(defaultBucket);
            }
        }

        if (documentFile.getUploadStoreType().equals(TOKEN_TYPES.DEFAULT_STORAGE)){

            // allow user to upload only to user S3 bucket
            documentFile.setBucket(defaultBucket);
        }

        // onBeforeUploadMetaInfoFile
        getFileRepositoryByName(documentFile).onBeforeUploadMetaInfoFile(documentFile);

        return documentFile;
    }

    public List<StorageFile> getListing(ExternalServiceToken token, String path) {
        StorageFileRepository repo = getFileRepositoryByName(token);
        path = repo.processListingPath(path);
        return repo.getListing(token, path);
    }

    public StorageFile findStorageFileById(String id) {
        StorageFile documentFile = defaultStorageService.findStorageFileById(id);

        // this method is called from gd direct link controller API
        checkThatUserCanAccessFileFromHisDomainInS3Storage(documentFile);
        return documentFile;
    }

    /**
     * @param file
     * @param documentFile
     * @return uploaded document meta info
     */
    public StorageFile uploadFile(File file, StorageFile documentFile) {
        documentFile.setFile(true);
        documentFile.setFolder(false);

        StorageFileRepository repo = getFileRepositoryByName(documentFile);
        repo.uploadFile(file, documentFile, currentUser.getCurrentUser(), currentUser.getDomain());
        repo.onAfterUploadFile(file, documentFile);

        updateConnectedFileObjects(documentFile);
        return documentFile;
    }

    private void updateConnectedFileObjects(StorageFile documentFile) {
        // update connected files statistics
        if (documentFile.getConnectedInfo() != null &&
                documentFile.getConnectedInfo().getConnectedTaskId() != null &&
                !documentFile.getConnectedInfo().getConnectedTaskId().equals("")) {
            taskRepository.addDocuemntFileToTaskById(documentFile.getConnectedInfo().getConnectedTaskId(), documentFile);
        }

        if (documentFile.getConnectedInfo() != null &&
                documentFile.getConnectedInfo().getConnectedCustomerId() != null
                && !documentFile.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.addDocuemntFileTOcustomerById(documentFile.getConnectedInfo().getConnectedCustomerId(), documentFile);
        }
    }


//    /**
//     * Parse file with Apache Tika and set Meta info to document
//     * This method works, but web server should process file
//     *
//     * @param file
//     * @param storageFile
//     * @return
//     */
//    public boolean parseAndSetMetaInfoForFile(File file, DocumentFile storageFile) {
//
//        InputStream input = null;
//        try {
//            input = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//        ContentHandler handler = new DefaultHandler();
//        Metadata metadata = new Metadata();
//        Parser parser = new AutoDetectParser();
//        ParseContext parseCtx = new ParseContext();
//
//        try {
//            parser.parse(input, handler, metadata, parseCtx);
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage());
//        } catch (SAXException e) {
//            throw new RuntimeException(e.getMessage());
//        } catch (TikaException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//        try {
//            input.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//
//        List<NameValueMap> metaInfo = new LinkedList<>();
//        String[] metadataNames = metadata.names();
//
//        for (String name : metadataNames) {
//            NameValueMap meta = new NameValueMap();
//            meta.setName(name);
//            meta.setValue(metadata.get(name));
//            metaInfo.add(meta);
//        }
//
//        storageFile.setMetaInfo(metaInfo);
//        return true;
//    }

    @BiqaCheckSecuredModifyObject
    public void deleteDocumentFile(StorageFile documentFile) {

        StorageFileRepository repository = getFileRepositoryByName(documentFile);

        if (documentFile.getUploadStoreType().equals(TOKEN_TYPES.DEFAULT_STORAGE)) {
            checkThatUserCanAccessFileFromHisDomainInS3Storage(documentFile);
        }

        repository.deleteDocumentFile(documentFile);
    }

    /**
     * Our amazon s3 storage is shred for all domains
     * so we must insure that users from one domain can not access files from users in another domain
     * Because user (has full access to his db) and can edit full StorageFile record in his database
     * @param storageFile
     */
    public void checkThatUserCanAccessFileFromHisDomainInS3Storage(StorageFile storageFile) {

        // if file is not uploaded - allow to modify / delete everyone
        if (storageFile.getFullName() == null && !storageFile.isUploaded()) {
            return;
        }

        // security ???
        // is it enough
        if (!storageFile.getFullName().startsWith(currentUser.getDomain().getDomain() + "/")) {
            throw new AccessDeniedException("You can download files only from your domain");
        }

    }

    public ByteArrayOutputStream downloadFile(StorageFile storageFile) {

        ExternalServiceToken externalServiceToken = null;

        if (!storageFile.getUploadStoreType().equals(TOKEN_TYPES.DEFAULT_STORAGE)) {
            externalServiceToken = externalServiceTokenRepository.findExternalServiceTokenById(storageFile.getUploadStoreID());

            if (externalServiceToken == null) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider_token");
            }
        }

        StorageFileRepository repository = getFileRepositoryByName(storageFile);

        // security check for shared default storage
        if (storageFile.getUploadStoreType().equals(TOKEN_TYPES.DEFAULT_STORAGE)) {
            checkThatUserCanAccessFileFromHisDomainInS3Storage(storageFile);
        }

        return repository.downloadFileWithToken(storageFile, externalServiceToken);
    }

}
