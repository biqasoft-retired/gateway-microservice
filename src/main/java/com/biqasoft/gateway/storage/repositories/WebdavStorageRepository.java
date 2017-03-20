/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.repositories;

import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.Domain;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.storage.StorageFileRepository;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebdavStorageRepository implements StorageFileRepository {

    private final StorageUserRepository documentFileRepository;

    @Autowired
    public WebdavStorageRepository(StorageUserRepository documentFileRepository) {
        this.documentFileRepository = documentFileRepository;
    }

    @Override
    public String getStorageName() {
        return TOKEN_TYPES.WEBDAV;
    }

    @Override
    public String processListingPath(String path) {
        if (StringUtils.isEmpty(path)) path = "/";
        return path;
    }

    @Override
    public ByteArrayOutputStream downloadFileWithToken(StorageFile documentFile, ExternalServiceToken externalServiceToken) {
        Sardine sardine = SardineFactory.begin(externalServiceToken.getLogin(), externalServiceToken.getToken());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            InputStream inputStream = sardine.get(externalServiceToken.getServer() + "/" + documentFile.getFullName().replace(" ", "%20"));
            IOUtils.copy(inputStream, stream);
            inputStream.close();
            stream.close();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return stream;
    }

    @Override
    public void createFolder(ExternalServiceToken externalServiceToken, StorageFile documentFile, String folderName, String path) {
        Sardine sardine = SardineFactory.begin(externalServiceToken.getLogin(), externalServiceToken.getToken());

        try {
            sardine.createDirectory(path + "/" + folderName);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteDocumentFile(StorageFile documentFile) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public StorageFile uploadFile(File file, StorageFile documentFile, UserAccount userAccoun, Domain domain) {
        ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("app.common");
        throw new InvalidRequestException("");
    }

    @Override
    public List<? extends StorageFile> getMetaInfo(String dir, ExternalServiceToken externalServiceToken) {
        return null;
    }

    @Override
    public List<StorageFile> getListing(ExternalServiceToken externalServiceToken, String path) {
        List<StorageFile> list = new ArrayList<>();
        Sardine sardine = SardineFactory.begin(externalServiceToken.getLogin(), externalServiceToken.getToken());
        List<DavResource> resources;

        try {
            resources = sardine.list(externalServiceToken.getServer() + "/" + path);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        for (DavResource res : resources) {
            StorageFile entity = new StorageFile();

            if (res.getContentType().equals("httpd/unix-directory")) {
                entity.setFolder(true);
            } else {
                entity.setFile(true);
            }

            entity.setFullName(res.getPath());
            entity.setName(res.getDisplayName());
            entity.setFileSize(res.getContentLength());
            entity.setMimeType(res.getContentType());
            entity.setCreatedInfo(new CreatedInfo(res.getCreation()));
            entity.setUploadStoreType(TOKEN_TYPES.WEBDAV);
            entity.setUploadStoreID(externalServiceToken.getId());

            list.add(entity);
        }

        return list;
    }
}
