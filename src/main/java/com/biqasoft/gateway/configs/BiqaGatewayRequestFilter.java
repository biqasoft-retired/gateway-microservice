/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.biqasoft.auth.CurrentUserMicroserviceRequestInterceptor.MICROSERVICE_REQUEST_ID_HEADER;
import static com.biqasoft.microservice.configs.LoggerConfigHelper.REQUEST_ID_LOGGER;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BiqaGatewayRequestFilter implements Filter {

    public BiqaGatewayRequestFilter() {
//        SecurityContextHolder securityContext = new SecurityContextHolder();
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        response.setHeader("Access-Control-Max-Age", "3600");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); // CORS, allow all use our API via Ajax

        // allow javascript client read this header
        response.addHeader("Access-Control-Expose-Headers", SYSTEM_CONSTS.X_biqa_OBJECT_EQUALS_HASH_HEADER.concat(", " + SYSTEM_CONSTS.X_biqa_Version_Hash_HEADER));

        // unique request ID
        response.addHeader(MICROSERVICE_REQUEST_ID_HEADER, MDC.get(REQUEST_ID_LOGGER));

        if (request.getMethod().equals("OPTIONS")) {
            response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Date, X-Date, Authorization".concat("," + SYSTEM_CONSTS.X_biqa_Version_Hash_HEADER));
            response.setHeader("X-Frame-Options", "DENY"); //SAMEORIGIN

            // if http request is options - do not process filters chain after
            // because we have user authentication filter and it will fail
            // with exception
            return;
        }

        // save user request locale to ThreadLocal, used in requests to microservices
        MDC.put("Accept-Language", request.getLocale().getLanguage());

        chain.doFilter(req, res);
    }

    public void destroy() {
    }

}