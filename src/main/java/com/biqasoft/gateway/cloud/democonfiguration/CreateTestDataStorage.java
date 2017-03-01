/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.democonfiguration;

import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.entity.constants.DOCUMENT_FILE;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.biqasoft.entity.constants.TOKEN_TYPES.DEFAULT_STORAGE;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 4/21/2016.
 * All Rights Reserved
 */
@Service
public class CreateTestDataStorage {

    private final DefaultStorageService defaultStorageService;
    private final MicroserviceDomainSettings microserviceDomainSettings;

    @Autowired
    public CreateTestDataStorage(DefaultStorageService defaultStorageService, MicroserviceDomainSettings microserviceDomainSettings) {
        this.defaultStorageService = defaultStorageService;
        this.microserviceDomainSettings = microserviceDomainSettings;
    }

    public void createFolders(){

        {
            StorageFile customerFolder = new StorageFile();
            customerFolder.setFolder(true);
            customerFolder.setParentId(DOCUMENT_FILE.ROOT_FOLDER_NAME);
            customerFolder.setName("Файлы клиентов");
            customerFolder.setAlias("CUSTOMER_FOLDER");
            customerFolder.setSecured(true);
            customerFolder.setUploadStoreType(DEFAULT_STORAGE);
            customerFolder.setUploaded(true);

            defaultStorageService.addStorageFile(customerFolder);

            // save customer folder id
            DomainSettings domainSettings = microserviceDomainSettings.findDomainSetting();
            domainSettings.setCustomerFolderId(customerFolder.getId());
            microserviceDomainSettings.updateDomainSettings(domainSettings);
        }

        defaultStorageService.checkBackupFolder();
    }

}
