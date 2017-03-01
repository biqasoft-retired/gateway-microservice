/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.microservice.database.MainDatabase;
import com.mongodb.CommandResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

/**
 * We have own mongodb check because
 * we can not use standard - it try to get all mongodb templates, including domainBasedMongoTemplate
 * which is request based(spring scope request) and can't be accessed and produce NPE
 *
 * Created by Nikita Bakaev, ya@nbakaev.ru on 4/18/2016.
 * All Rights Reserved
 */
@Component
public class MongoDBHealthIndicator  extends AbstractHealthIndicator {

    private final MongoOperations ops;

    @Autowired
    public MongoDBHealthIndicator(@MainDatabase MongoOperations ops) {
        this.ops = ops;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        CommandResult result = ops.executeCommand("{ buildInfo: 1 }");
        builder.up().withDetail("version", result.getString("version"));
    }

}
