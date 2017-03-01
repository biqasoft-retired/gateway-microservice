/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.microservice.database.TenantDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import com.biqasoft.entity.objects.CustomObjectDataSegment;

import java.util.List;

/**
 * This is real custom objects
 */
@Service
public class CustomObjectsDataSegmentsRepository {

    private final MongoOperations ops;

    @Autowired
    public CustomObjectsDataSegmentsRepository(@TenantDatabase MongoOperations ops) {
        this.ops = ops;
    }

    public List<CustomObjectDataSegment> getAllSegments() {
        return ops.findAll(CustomObjectDataSegment.class);
    }

    public CustomObjectDataSegment addSegment(CustomObjectDataSegment customObjectsDataRepository) {
        ops.insert(customObjectsDataRepository);
        return customObjectsDataRepository;
    }

    public CustomObjectDataSegment updateSegment(CustomObjectDataSegment customObjectsDataRepository) {
        ops.save(customObjectsDataRepository);
        return customObjectsDataRepository;
    }

    public void deleteSegment(String id) {
        CustomObjectDataSegment segment = new CustomObjectDataSegment();
        segment.setId(id);

        ops.remove(segment);
    }

}
