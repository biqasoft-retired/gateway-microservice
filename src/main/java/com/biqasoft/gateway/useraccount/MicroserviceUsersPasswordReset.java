package com.biqasoft.gateway.useraccount;

import com.biqasoft.gateway.useraccount.dto.ResetPasswordTokenDTO;
import com.biqasoft.entity.dto.useraccount.PasswordResetDTO;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;

import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;
import org.springframework.http.HttpMethod;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/18/2016
 *         All Rights Reserved
 */
@Microservice("users")
public interface MicroserviceUsersPasswordReset {

    @MicroMapping(path = "/v1/users/auth/password/reset/create_token", method = HttpMethod.POST)
    void create(ResetPasswordTokenDTO resetPasswordTokenDao);

    @MicroMapping(path = "/v1/users/auth/password/domain/reset_password_by_token", method = HttpMethod.POST)
    void removeAndResetPasswordTokenDao(ResetPasswordTokenDTO UserAccountGroup);

    @MicroMapping(path = "/v1/users/auth/password/domain/change_password", method = HttpMethod.PUT)
    PasswordResetDTO resetPasswordForUserInDomain(UserAccount userAccount);

}
