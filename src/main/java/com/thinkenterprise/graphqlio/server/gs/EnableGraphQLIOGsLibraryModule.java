package com.thinkenterprise.graphqlio.server.gs;


import java.lang.annotation.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(GraphQLIOLibraryGsConfiguration.class)
@Configuration
public @interface EnableGraphQLIOGsLibraryModule {
}