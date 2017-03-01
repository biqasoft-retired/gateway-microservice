package com.biqasoft.gateway.diffs;

import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroPathVar;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/18/2016
 *         All Rights Reserved
 */
@Microservice("users")
public interface MicroserviceUsersDiffRepository {

    @MicroMapping("/v1/diff/history/objects/class/{className}/id/{id}")
    JsonNode findChanges(@MicroPathVar("className") String className, @MicroPathVar("id") String id);

}
