/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.audit.object.customfield.CustomObjectUtils;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.entity.filters.CustomObjectsDataFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import com.biqasoft.gateway.objects.custom.template.CustomObjectsRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * This is real custom objects
 */
@Service
public class CustomObjectsDataRepositoryImpl implements CustomObjectsDataRepository {

    private final MongoOperations ops;
    private final CustomObjectsRepository customObjectsRepository;
    private final BiqaObjectFilterService biqaObjectFilterService;

    @Autowired
    public CustomObjectsDataRepositoryImpl(CustomObjectsRepository customObjectsRepository, @TenantDatabase MongoOperations ops,
                                           BiqaObjectFilterService biqaObjectFilterService) {
        this.customObjectsRepository = customObjectsRepository;
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    @Override
    @BiqaAddObject
    @BiqaAuditObject
    public CustomObjectData addCustomObjectBlank(CustomObjectData customObject) {
        // check that object have collectionId
        CustomObjectUtils.getCollectionNameFromCustomObject(customObject);

        CustomObjectTemplate customObject1ObjectTemplate = customObjectsRepository.findCustomObjectById(customObject.getCollectionId());
        if (StringUtils.isEmpty(customObject1ObjectTemplate.getCollectionId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.add.no_template");
        }

        // add fields from customObjectTemplate
        if (customObject1ObjectTemplate.getCustomFields() != null && customObject1ObjectTemplate.getCustomFields().size() > 0) {
            customObject.setCustomFields(customObject1ObjectTemplate.getCustomFields());
        }

        ops.insert(customObject, CustomObjectUtils.getCollectionNameFromCustomObject(customObject));
        return customObject;
    }

    @Override
    public CustomObjectData findCustomObjectById(CustomObjectData customObject) {
        return ops.findOne(Query.query(Criteria.where("id").is(customObject.getId()) ), CustomObjectData.class, CustomObjectUtils.getCollectionNameFromCustomObject(customObject));
    }

    @BiqaAuditObject
    @Override
    public CustomObjectData updateCustomObject(CustomObjectData customObject) {
        ops.save(customObject, CustomObjectUtils.getCollectionNameFromCustomObject(customObject));
        return customObject;
    }

    @Override
    public void deleteCustomObject(CustomObjectData customObject) {
        ops.remove(customObject, CustomObjectUtils.getCollectionNameFromCustomObject(customObject));
    }

    @Override
    public CustomObjectData findCustomObjectByIdAndCollectionId(String objectId, String collectionId) {
        CustomObjectData customObject = new CustomObjectData();
        customObject.setId(objectId);
        customObject.setCollectionId(collectionId);
        customObject = findCustomObjectById(customObject);
        return customObject;
    }

    @Override
    public void deleteCustomObjectWithIdAndCollectionId(String objectId, String collectionId) {
        CustomObjectData customObject = findCustomObjectByIdAndCollectionId(objectId, collectionId);
        ops.remove(customObject, CustomObjectUtils.getCollectionNameFromCustomObject(customObject));
    }

    @Override
    public BiqaPaginationResultList<CustomObjectData> getCustomObjectTemplateFromFilter(CustomObjectsDataFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        String collectionName = filter.getCollectionId();
        if (StringUtils.isEmpty(collectionName)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.template.from_builder");
        }
        collectionName = CustomObjectUtils.CUSTOM_OBJECT_COLLECTION_PREFIX + collectionName;
        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, CustomObjectData.class, ops, collectionName);
    }

}
