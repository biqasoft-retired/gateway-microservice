/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.controllers;

import com.biqasoft.common.exceptions.InternalSeverErrorProcessingRequestException;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.DOCUMENT_FILE;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.dto.httpresponse.SampleDataResponse;
import com.biqasoft.entity.filters.StorageFileFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.gateway.externalservice.ExternalServiceTokenRepository;
import com.biqasoft.gateway.storage.repositories.StorageUserRepository;
import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.storage.s3.AmazonS3FileRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.biqasoft.entity.constants.TOKEN_TYPES.DEFAULT_STORAGE;
import static com.biqasoft.storage.s3.DefaultS3FileRepository.BACKUP_FOLDER_ALIAS;

@Api(value = "Storage")
@Secured(value = {SYSTEM_ROLES.DOCUMENTS_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/storage")
public class StorageController {

    private final AmazonS3FileRepository awsAccount;
    private final Integer defaultDirectLinkTTL;
    private final DefaultStorageService defaultStorageService;
    private final StorageUserRepository documentFileRepository;
    private final ExternalServiceTokenRepository externalServiceTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    @Autowired
    public StorageController(AmazonS3FileRepository amazonS3FileRepository, DefaultStorageService defaultStorageService,
                             StorageUserRepository documentFileRepository, ExternalServiceTokenRepository externalServiceTokenRepository,
                             @Value("${aws.s3.default.ttl.direct.link}") Integer defaultDirectLinkTTL) {
        this.awsAccount = amazonS3FileRepository;
        this.defaultStorageService = defaultStorageService;
        this.documentFileRepository = documentFileRepository;
        this.externalServiceTokenRepository = externalServiceTokenRepository;
        this.defaultDirectLinkTTL = defaultDirectLinkTTL;
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all uploaded files and folders", notes = "only uploaded files and folders will be shown")
    @RequestMapping(method = RequestMethod.GET)
    public List<StorageFile> getAllDocuments() {
        StorageFileFilter builder = new StorageFileFilter();
        builder.setOnlyUploaded(true);

        BiqaPaginationResultList<StorageFile> biqaPaginationResultList = defaultStorageService.getStorageFileByFilter(builder);
        return biqaPaginationResultList.getResultedObjects();
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get list of files in folder and token. used for google drive, dropbox etc...; if you want system default storage - tokenID should be 'DEFAULT' ")
    @RequestMapping(value = "listing", method = RequestMethod.GET)
    public List<StorageFile> getAllFiles(@RequestParam(value = "path") String path, @RequestParam(value = "tokenID") String id) {
        if (id == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.no_provider");
        }
        ExternalServiceToken externalServiceToken;

        if (DEFAULT_STORAGE.equals(id)) {
            if (StringUtils.isEmpty(path) || path.equals("undefined")) {
                path = DOCUMENT_FILE.ROOT_FOLDER_NAME;
            }
            externalServiceToken = new ExternalServiceToken();
            externalServiceToken.setType(DEFAULT_STORAGE);
        } else {
            externalServiceToken = externalServiceTokenRepository.findExternalServiceTokenById(id);
        }

        return documentFileRepository.getListing(externalServiceToken, path);
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get document meta info by Id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public StorageFile detailedDocumentFileInfo(@PathVariable("id") String id) {
        return documentFileRepository.findStorageFileById(id);
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_DOWNLOAD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get public URL of document by ID")
    @RequestMapping(value = "get_document_url_by_id/{id}", method = RequestMethod.GET)
    public SampleDataResponse getDocumentURLById(@PathVariable("id") String id) {
        int ttl = this.defaultDirectLinkTTL;
        StorageFile documentFile = documentFileRepository.findStorageFileById(id);

        if (documentFile == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.download.no_file");
        }

        return awsAccount.getDirectLinkForFileByBucketAndFullNameAndTTL(documentFile.getBucket(), documentFile.getFullName(), ttl);
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_DOWNLOAD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete file/folder")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void deleteDocumentFile(@RequestBody StorageFile documentFile) {
        documentFileRepository.deleteDocumentFile(documentFile);
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_DOWNLOAD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "download file")
    @RequestMapping(value = "download_file", method = RequestMethod.POST)
    public void downloadFile(@RequestBody StorageFile documentFile, HttpServletResponse response) {
        response.setContentType(documentFile.getMimeType());
        response.setContentLengthLong(documentFile.getFileSize());

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            documentFileRepository.downloadFile(documentFile).writeTo(outputStream);
            response.flushBuffer();
        } catch (IOException e) {
            logger.warn("Error downloading file", e);
        }
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "add meta info to new document",
            notes = "creating new document, in result you get ID which you can later use to upload new file with method 'sendWithPreId' ")
    @RequestMapping(value = "upload/send_meta_information", method = RequestMethod.POST)
    public StorageFile getMetaInfoForUploadingFile(@RequestBody StorageFile documentFile, HttpServletResponse response) {

        // this is special system alias
        // and giving ability to create document with this alias can create security issue
        if (BACKUP_FOLDER_ALIAS.equals(documentFile.getAlias())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("app.wrong.alias.deny");
        }

        documentFileRepository.processMetaInfoForUploadingFile(documentFile);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return documentFile;
    }

    @ApiOperation(value = "files")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<StorageFile> getDocumentsByFilter(@RequestBody StorageFileFilter builder) {
        return defaultStorageService.getStorageFileByFilter(builder);
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all storage providers")
    @RequestMapping(value = "providers", method = RequestMethod.POST)
    public List<ExternalServiceToken> getMetaInfoForUploadingFile() {
        return externalServiceTokenRepository.findAllStorageTokens();
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update meta information of document")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public StorageFile updateDocumentFile(@RequestBody StorageFile documentFile) {
        return defaultStorageService.updateStorageFile(documentFile);
    }

    @Secured(value = {SYSTEM_ROLES.DOCUMENTS_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "upload new document",
            notes = "upload new document, firstly you should get new document ID from 'POST sendMetaInformation'  ")
    @RequestMapping(value = "upload/send_with_pre_id/{id}", method = RequestMethod.POST)
    public StorageFile sendWithPreId(@RequestParam("file") MultipartFile fileSource, HttpServletResponse response, @PathVariable("id") String id) {
        StorageFile documentFile = documentFileRepository.findStorageFileById(id);

        if (documentFile.isUploaded()) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("storage.wrong.already_uploaded");
        }

        documentFile.setName(fileSource.getOriginalFilename());
        File file;

        // note: don't use just `file2 = new File(storage.getId());`
        // because you fill have wrong absolute path in file
        // and then will not be able to use this `File`
        try {
            file = File.createTempFile(documentFile.getId(), ".data");
            fileSource.transferTo(file);

        } catch (IOException e) {
            throw new InternalSeverErrorProcessingRequestException(e.getMessage());
        }
        documentFile = documentFileRepository.uploadFile(file, documentFile);

        response.setStatus(HttpServletResponse.SC_CREATED);
        if (file != null) {
            file.delete();
        }
        return documentFile;
    }

}
