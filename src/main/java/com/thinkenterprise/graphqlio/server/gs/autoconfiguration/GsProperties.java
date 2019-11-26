package com.thinkenterprise.graphqlio.server.gs.autoconfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "graphqlio.server")
public class GsProperties {

    private String schemaLocationPattern;
    
    private String endpoint;

    
    public void setSchemaLocationPattern(String schemaLocationPattern) {
        this.schemaLocationPattern=schemaLocationPattern;
    }

    public String getSchemaLocationPattern() {
        return this.schemaLocationPattern;
    }
    
    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint=endpoint;
    }


}
