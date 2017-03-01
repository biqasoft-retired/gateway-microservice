/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.gateway.configs.exceptionhandler;

import com.biqasoft.common.exceptions.dto.ErrorResource;
import com.biqasoft.auth.exception.auth.BiqaAuthenticationLocalizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handle user basic auth error in API
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/10/2016
 *         All Rights Reserved
 */
@Component
public class AuthExceptionHandler extends BasicAuthenticationEntryPoint {

    private final String realmName = "biqa";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        ErrorResource error;
        if (httpServletResponse.isCommitted()){
            return;
        }

        if (e instanceof BiqaAuthenticationLocalizedException) {
            error = ((BiqaAuthenticationLocalizedException) e).getErrorResource();
        } else {
            error = new ErrorResource("Authentication Failed", e.getMessage());
            error.setIdErrorMessage(e.getMessage());
            error.setEnglishErrorMessage(e.getMessage());
        }

        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.setStatus(401);
        httpServletResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realmName + "\"");
        httpServletResponse.getWriter().append(objectMapper.writeValueAsString(error)).flush();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.realmName, "realmName must be specified");
    }

}
