package com.biqasoft.gateway.useraccount;

import com.biqasoft.users.domain.useraccount.UserAccountGroup;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroPathVar;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;

import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/18/2016
 *         All Rights Reserved
 */
@Microservice("users")
public interface MicroserviceUserAccountGroup {

    @MicroMapping(path = "/v1/users/group", method = HttpMethod.POST)
    UserAccountGroup create(UserAccountGroup UserAccountGroup);

    @MicroMapping(path = "/v1/users/group/{id}", method = HttpMethod.DELETE)
    void delete(@MicroPathVar("id") String UserAccountGroup);

    @MicroMapping(path = "/v1/users/group", method = HttpMethod.GET)
    List<UserAccountGroup> findAll();

    @MicroMapping(path = "/v1/users/group/", method = HttpMethod.PUT)
    UserAccountGroup update(UserAccountGroup UserAccountGroup);

    @MicroMapping(path = "/v1/users/group/{id}", method = HttpMethod.GET)
    UserAccountGroup findById(@MicroPathVar("id") String UserAccountGroup);

}
