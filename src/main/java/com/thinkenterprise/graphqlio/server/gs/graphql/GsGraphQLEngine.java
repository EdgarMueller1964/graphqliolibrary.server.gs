package com.thinkenterprise.graphqlio.server.gs.graphql;

import static graphql.GraphQL.newGraphQL;

import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Component
public class GsGraphQLEngine {

	final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();
	
    private GraphQL graphQL = null;
    
    public GraphQL getGraphQLEngine() {
    	return graphQL;
    }
    
    public GraphQL create (GraphQLSchema schema) {
		if ( schema != null) {
			log.info("Creating GraphQLSchema");	
			graphQL = newGraphQL(schema).build();
			
			return graphQL;
		}
		return graphQL;    	
    }
	
}