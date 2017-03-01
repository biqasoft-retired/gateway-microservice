/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.auth.GatewayAuthenticationProvider;
import com.biqasoft.auth.exception.auth.ThrowAuthExceptionHelper;
import com.biqasoft.entity.core.useraccount.UserAccount;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UserAccount authentication ip address filter
 * for current authenticated user with 'ipPattern' field
 * and domain active/inactive control
 */
@Component
@Configurable
public class BiqaGatewayIpAndDomainFilter implements Filter {

    private final CurrentUser currentUser;
    private final GatewayAuthenticationProvider gatewayAuthenticationProvider;
    private final AuthExceptionHandler authExceptionHandler;

    @Autowired
    public BiqaGatewayIpAndDomainFilter(final CurrentUser currentUser, final GatewayAuthenticationProvider gatewayAuthenticationProvider, AuthExceptionHandler authExceptionHandler) {
        this.currentUser = currentUser;
        this.gatewayAuthenticationProvider = gatewayAuthenticationProvider;
        this.authExceptionHandler = authExceptionHandler;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private void checkUserIpPattern(String remoteAddr) {
        if (currentUser.getCurrentUser() != null) {
            UserAccount userAccount = currentUser.getCurrentUser();

            // check for allowed IP address for user regexp pattern
            if (!StringUtils.isEmpty(userAccount.getIpPattern())) {
                Pattern pattern = Pattern.compile(userAccount.getIpPattern());
                Matcher matcher = pattern.matcher(remoteAddr);

                if (!matcher.matches()) {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.ip_deny");
                }
            }

            // check for active domain
            if (!currentUser.getDomain().isActive()) {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.domain_inactive");
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        if (currentUser.getCurrentUser() != null) {
            checkUserIpPattern(request.getRemoteAddr());
        } else {
            // try auth http request param
            String authorizationFromHttpRequestParamAsHeader = GatewayAuthenticationProvider.getAuthorizationFromHttpRequestParamAsHeader(request);
            if (authorizationFromHttpRequestParamAsHeader != null) {
                try {
                    gatewayAuthenticationProvider.authenticateUser(null, authorizationFromHttpRequestParamAsHeader, new WebAuthenticationDetails(request));
                    checkUserIpPattern(request.getRemoteAddr());
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