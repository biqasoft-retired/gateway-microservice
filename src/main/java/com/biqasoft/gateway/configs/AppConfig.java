/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.configs;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.MultipartConfigElement;

@Configuration
public class AppConfig {

    // that's to work with scope 'request'
    // just need to spring boot ( not vanilla tomcat )
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 128KB
        factory.setMaxFileSize("512MB");
        factory.setMaxRequestSize("512MB");
        return factory.createMultipartConfig();
    }

    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Bean
    RestTemplate restTemplate() throws Exception {
        RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

}