/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import com.biqasoft.auth.GatewayAuthenticationProvider;
import com.biqasoft.gateway.configs.exceptionhandler.AuthExceptionHandler;
import com.biqasoft.gateway.configs.exceptionhandler.ExceptionTranslationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private GatewayAuthenticationProvider gatewayAuthenticationProvider;

    @Autowired
    private BiqaGatewayAuthFilter biqaGatewayAuthFilter;

    @Autowired
    private AuthExceptionHandler authExceptionHandler;

    public SpringSecurityConfig() {
        super(true);
    } //disable auto configuration security filters

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(biqaGatewayAuthFilter, BasicAuthenticationFilter.class);
        http.addFilterAfter(new ExceptionTranslationFilter(authExceptionHandler), BasicAuthenticationFilter.class);

        http.securityContext();
        http.anonymous();

        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll() // allow Ajax

                .antMatchers("/v1/calendar/public/**").permitAll()
                .antMatchers("/v1/constants/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").permitAll() // root public health check
                .antMatchers(HttpMethod.POST, "/oauth/obtain_access_code/**").permitAll()
                .antMatchers("/v2/api-docs/**").permitAll() // swagger
                .antMatchers(HttpMethod.POST, "/landing_page/**").permitAll()
                .antMatchers(HttpMethod.POST, "/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/public/**").permitAll()
                .antMatchers("/v1/public/**").permitAll()
//                .antMatchers("/internal/**").hasRole("ROOT_USER")
                .antMatchers("/internal/**").permitAll()
                .antMatchers("/**").authenticated()
        .and()
        .httpBasic().authenticationEntryPoint(authExceptionHandler);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
         auth.authenticationProvider(gatewayAuthenticationProvider);
    }

}
