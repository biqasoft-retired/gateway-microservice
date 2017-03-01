/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CustomObjectsDataFilter;
import com.biqasoft.entity.core.objects.CustomObjectData;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/27/2016
 *         All Rights Reserved
 */
public interface CustomObjectsDataRepository {

    /**
     * This method add blank customObject with fields
     *
     * @param customObject
     * @return
     */
    CustomObjectData addCustomObjectBlank(CustomObjectData customObject);

    CustomObjectData findCustomObjectById(CustomObjectData customObject);

    CustomObjectData updateCustomObject(CustomObjectData customObject);

    void deleteCustomObject(CustomObjectData customObject);

    CustomObjectData findCustomObjectByIdAndCollectionId(String objectId, String collectionId);

    void deleteCustomObjectWithIdAndCollectionId(String objectId, String collectionId);

    BiqaPaginationResultList<CustomObjectData> getCustomObjectTemplateFromFilter(CustomObjectsDataFilter filter);
}
