/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.template;

import com.biqasoft.audit.object.customfield.CustomFieldProcessingService;
import com.biqasoft.audit.object.customfield.CustomObjectUtils;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.CUSTOM_OBJECTS_PRINTABLE_TYPES;
import com.biqasoft.entity.filters.CustomObjectsFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.objects.CustomObjectPrintableTemplate;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * This is TEMPLATES for custom objects,
 * which save meta information about custom objects fields... etc
 */
@Service
public class CustomObjectsRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final CustomFieldProcessingService customFieldProcessingRepository;

    @Autowired
    public CustomObjectsRepository(@TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService, CustomFieldProcessingService customFieldProcessingRepository) {
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.customFieldProcessingRepository = customFieldProcessingRepository;
    }

    /**
     * we should create indexes manually because store objects in different collections
     * http://stackoverflow.com/questions/17914725/indexed-annotation-ignored-when-using-named-collections
     * http://stackoverflow.com/questions/33127967/create-indexes-for-search-using-mongotemplate
     *
     * @param colName name of collection where create default indexes
     */
    public void createIndexes(String colName) {
        if (!ops.collectionExists(colName)) {
            ops.createCollection(colName);

            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("name")
                    .onField("description")
                    .onField("customFields.value.stringVal")
                    .build();

            try {
                ops.indexOps(colName).ensureIndex(textIndex);
                ops.indexOps(colName).ensureIndex(new Index().on("archived", Sort.Direction.ASC));
                ops.indexOps(colName).ensureIndex(new Index().on("createdInfo", Sort.Direction.ASC));
                ops.indexOps(colName).ensureIndex(new Index().on("createdInfo.createdById", Sort.Direction.ASC));
                ops.indexOps(colName).ensureIndex(new Index().on("createdInfo.createdDate", Sort.Direction.ASC));

            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        }
    }

    @BiqaAddObject(forceAddCustomField = false)
    @BiqaAuditObject
    public CustomObjectTemplate addCustomObject(CustomObjectTemplate customObjectTemplate) {
        customObjectTemplate.setCollectionId(customObjectTemplate.getId());
        try {

            Resource resource = new ClassPathResource("templates/default_customobject_template.hbs");
            InputStream resourceInputStream = resource.getInputStream();

            CustomObjectPrintableTemplate htmlBasedPrintableTemplate = new CustomObjectPrintableTemplate();
            {
                htmlBasedPrintableTemplate.setName("шаблон по умолчанию");
                htmlBasedPrintableTemplate.setDescription("шаблон по умолчанию со всеми атрибутами в HTML формате");
                htmlBasedPrintableTemplate.setExtension(".html");
                htmlBasedPrintableTemplate.setType(CUSTOM_OBJECTS_PRINTABLE_TYPES.HANDLEBARS);
                htmlBasedPrintableTemplate.setMimeType("text/html");
                htmlBasedPrintableTemplate.setData(IOUtils.toString(resourceInputStream));
                customObjectTemplate.getPrintableTemplates().add(htmlBasedPrintableTemplate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ops.insert(customObjectTemplate);
        String colName = CustomObjectUtils.CUSTOM_OBJECT_COLLECTION_PREFIX + customObjectTemplate.getCollectionId();
        createIndexes(colName);

        return customObjectTemplate;
    }

    public CustomObjectTemplate findCustomObjectById(String id) {
        return this.ops.findOne(Query.query(Criteria.where("id").is(id)), CustomObjectTemplate.class);
    }

    public List<CustomObjectTemplate> findAll() {
        return this.ops.findAll(CustomObjectTemplate.class);
    }

    @BiqaAuditObject
    public CustomObjectTemplate updateCustomObject(CustomObjectTemplate customer) {
        CustomObjectTemplate oldTemplate = findCustomObjectById(customer.getId());

        customFieldProcessingRepository.processFields(
                oldTemplate.getCustomFields(),
                customer.getCustomFields(),
                CustomObjectUtils.CUSTOM_OBJECT_COLLECTION_PREFIX + customer.getCollectionId()
        );

        ops.save(customer);
        return customer;
    }

    public boolean deleteCustomObject(String id) {
        CustomObjectTemplate customObject = new CustomObjectTemplate();
        customObject.setId(id);

        ops.remove(customObject);
        return true;
    }

    public BiqaPaginationResultList<CustomObjectTemplate> getCustomObjectTemplateFromBuilder(CustomObjectsFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, CustomObjectTemplate.class, ops);
    }

}
