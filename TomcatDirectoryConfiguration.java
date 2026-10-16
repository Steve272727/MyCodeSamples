package com.example.mock;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
//Doesn't work in Tomcat 11
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//Tomcat 11:
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.catalina.webresources.DirResourceSet;

//This works!!!
@Configuration
public class TomcatDirectoryConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
            directoryListingCustomizer() {

        return factory -> factory.addContextCustomizers(
            (Context context) -> {

                // Find Tomcat's default servlet.
                Wrapper defaultServlet =
                    (Wrapper) context.findChild("default");

                if (defaultServlet == null) {
                    throw new IllegalStateException(
                        "Tomcat default servlet not found");
                }

                // Enable directory listings.
                defaultServlet.addInitParameter(
                    "listings", "true");

                WebResourceRoot resources = new StandardRoot(context);

				//TODO: add config settings for:
				//Directory path: D:/Data/Code/workspace/mock-sftp-smtp-server/mock-sftp-smtp-server/data                
				//Directory url: /files/*
                
                resources.addPreResources(
                    new DirResourceSet(
                        resources,
                        "/files",
                        "D:/Data/Code/workspace/mock-sftp-smtp-server/mock-sftp-smtp-server/data",
                        "/"
                    )
                );

                context.setResources(resources);                
                
                // Map the servlet to /files/*
                defaultServlet.addMapping("/files/*");
            });
    }
}