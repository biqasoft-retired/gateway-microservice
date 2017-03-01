/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs.exceptionhandler;

import com.biqasoft.common.exceptions.InternalSeverErrorProcessingRequestException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/5/2016
 *         All Rights Reserved
 */
@ControllerAdvice
public class InternalErrorExceptionHandler {

    @ExceptionHandler(InternalSeverErrorProcessingRequestException.class)
    public void handleInternalSeverErrorProcessingRequestException(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(com.biqasoft.microservice.communicator.exceptions.CannotResolveHostException.class)
    public void handleCannotResolveHostException(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}

