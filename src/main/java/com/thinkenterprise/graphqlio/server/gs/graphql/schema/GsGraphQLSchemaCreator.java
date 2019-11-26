package com.thinkenterprise.graphqlio.server.gs.graphql.schema;

import graphql.schema.GraphQLSchema;

public interface GsGraphQLSchemaCreator {

	GraphQLSchema create();
	
	GraphQLSchema getGraphQLSchema();
	
}
