package com.biqasoft.gateway.configs;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Created by Nikita on 14.08.2016.
 */
@Configuration
public class ServerContext {

//    @Bean
//    public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory(){
//        TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory = new TomcatEmbeddedServletContainerFactory();
//        tomcatEmbeddedServletContainerFactory.setPersistSession(false);
//        return tomcatEmbeddedServletContainerFactory;
//    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            servletContext.setSessionTrackingModes(Collections.emptySet()); // disable cookie generation Set-Cookie: JSESSIONID
        };

    }
}