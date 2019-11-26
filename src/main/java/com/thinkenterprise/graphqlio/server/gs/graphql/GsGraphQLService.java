package com.thinkenterprise.graphqlio.server.gs.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.thinkenterprise.graphqlio.server.gs.graphql.schema.GsGraphQLSchemaCreator;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Service
@Scope("singleton")
public class GsGraphQLService {

	final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	
	@Autowired
	private GsGraphQLSchemaCreator graphQLSchemaCreator;
		
	@Autowired
	private GsGraphQLEngine gsGgraphQLEngine;

	
	private GraphQLSchema graphQLSchema = null;
	private GraphQL graphQL = null;
		
	public boolean start() {
		log.info("Creating GraphQL instance from schema");

		graphQLSchema = graphQLSchemaCreator.create();
		if (graphQLSchema != null) {
			graphQL = gsGgraphQLEngine.create(graphQLSchema);			
		}
		
		return graphQLSchema != null && graphQL != null;
	}
	
	/// stop the service
	public void stop() {
		//// any cleanup routine....
	}
	

}
