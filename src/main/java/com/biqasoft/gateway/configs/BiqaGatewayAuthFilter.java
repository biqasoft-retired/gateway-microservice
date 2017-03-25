/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.auth.GatewayAuthenticationProvider;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.gateway.configs.exceptionhandler.AuthExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * UserAccount authentication ip address filter
 * for current authenticated user with 'ipPattern' field
 * and domain active/inactive control
 */
@Component
@Configurable
public class BiqaGatewayAuthFilter implements Filter {

    private final CurrentUser currentUser;
    private final GatewayAuthenticationProvider gatewayAuthenticationProvider;
    private final AuthExceptionHandler authExceptionHandler;

    @Autowired
    public BiqaGatewayAuthFilter(final CurrentUser currentUser, final GatewayAuthenticationProvider gatewayAuthenticationProvider, AuthExceptionHandler authExceptionHandler) {
        this.currentUser = currentUser;
        this.gatewayAuthenticationProvider = gatewayAuthenticationProvider;
        this.authExceptionHandler = authExceptionHandler;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        if (currentUser.getCurrentUser() == null) {
            // try auth http request param
            String authorizationFromHttpRequestParamAsHeader = GatewayAuthenticationProvider.getAuthorizationFromHttpRequestParamAsHeader(request);

            if (authorizationFromHttpRequestParamAsHeader == null) {
                String authorization = ((HttpServletRequest) req).getHeader("Authorization");
                if (!StringUtils.isEmpty(authorization) && authorization.startsWith("Biqa ")) {
                    authorizationFromHttpRequestParamAsHeader = authorization;
                }
            }

            if (authorizationFromHttpRequestParamAsHeader != null) {
                try {
                    gatewayAuthenticationProvider.authenticateUser(null, authorizationFromHttpRequestParamAsHeader, new WebAuthenticationDetails(request));
                } catch (AuthenticationException e) {
                    authExceptionHandler.commence(request, response, e);
                    SecurityContextHolder.clearContext();
                    return;
                }
            }

        }

        chain.doFilter(req, res);
    }

    public void destroy() {
    }

}