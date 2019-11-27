package com.thinkenterprise.graphqlio.server.gs.autoconfiguration;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.thinkenterprise.graphqlio.server.gs.execution.GsExecutionStrategy;
import com.thinkenterprise.graphqlio.server.gs.graphql.GsGraphQLExecution;
import com.thinkenterprise.graphqlio.server.gs.graphql.GsGraphQLService;
import com.thinkenterprise.graphqlio.server.gs.graphql.schema.GsGraphQLSchemaCreator;
import com.thinkenterprise.graphqlio.server.gs.graphql.schema.GsGraphQLSimpleSchemaCreator;
import com.thinkenterprise.graphqlio.server.gs.handler.GsWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableConfigurationProperties(GsAutoConfiguration.class)
@ConfigurationProperties(prefix = "graphqlio.server")
@EnableWebSocket
public class GsAutoConfiguration implements WebSocketConfigurer {

	@Autowired
	private GsProperties gsProperties;
	
	
	@Autowired
	private GsWebSocketHandler handler;

	private RedisTemplate<String, String> redisTemplate;
	
	@Bean
	@ConditionalOnMissingBean
	public GsGraphQLSchemaCreator gsGraphQLSchemaCreator() {
		return new GsGraphQLSimpleSchemaCreator();
	}

	@Bean
	@ConditionalOnMissingBean
	public GsGraphQLService gsGraphQLService() {
		return new GsGraphQLService();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public GsExecutionStrategy graphQLIOQueryExecutionStrategy() {
		return new GsGraphQLExecution();
	}
	
		
	@Bean
	public ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).registerModule(new Jdk8Module());
        
        InjectableValues.Std injectableValues = new InjectableValues.Std();
        injectableValues.addValue(ObjectMapper.class, mapper);
        mapper.setInjectableValues(injectableValues);

        return mapper;
    }

	
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {	 
    	registry.addHandler(this.handler, gsProperties.getEndpoint());   
	}
    
}