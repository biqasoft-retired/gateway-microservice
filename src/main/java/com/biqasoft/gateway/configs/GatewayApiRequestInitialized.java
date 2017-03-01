/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.microservice.configs.LoggerConfigHelper;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import static com.biqasoft.microservice.configs.LoggerConfigHelper.REQUEST_ID_LOGGER;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/20/2016
 *         All Rights Reserved
 */
@Component
public class GatewayApiRequestInitialized implements ServletRequestListener {

    /**
     * REQUEST INITIALIZED
     *
     * @param servletRequestEvent
     */
    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        // MDC.clear(); // we clear on request destroy

        // in non public microservice we get request id from headers, but this microservice is public
        // so, potentially we do not want to allow user to write to console
        // or we can get hash (such as md5) to prevent attacks
        // on the other hand, if we will send request id from client(browser), we can match logs from browser and make powerful APM further
        MDC.put(REQUEST_ID_LOGGER, LoggerConfigHelper.generateRequestId());

        HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
        MDC.put("Authorization", request.getHeader("Authorization")); // forward auth header to microservices
    }

    /**
     * REQUEST DESTROYED
     *
     * @param servletRequestEvent
     */
    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        MDC.clear();
    }
}
