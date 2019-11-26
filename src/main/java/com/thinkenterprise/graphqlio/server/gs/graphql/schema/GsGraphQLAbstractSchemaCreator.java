package com.thinkenterprise.graphqlio.server.gs.graphql.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.thinkenterprise.graphqlio.server.gs.autoconfiguration.GsProperties;
import com.thinkenterprise.graphqlio.server.gtt.types.GttDateType;
import com.thinkenterprise.graphqlio.server.gtt.types.GttJsonType;
import com.thinkenterprise.graphqlio.server.gtt.types.GttUuidType;
import com.thinkenterprise.graphqlio.server.gtt.types.GttVoidType;

import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;

public abstract class GsGraphQLAbstractSchemaCreator implements GsGraphQLSchemaCreator {

	List<GraphQLScalarType> scalarTypes = new ArrayList<>();
	
	@Autowired
	private GsProperties gsProperties;
		
		
	GraphQLSchema graphQLSchema = null;
		
	@Override
	public GraphQLSchema getGraphQLSchema() {
		return graphQLSchema;
	}
		
	protected void initScalarTypes() {
		scalarTypes.add(new GttUuidType());
		scalarTypes.add(new GttDateType());
		scalarTypes.add(new GttJsonType());
		scalarTypes.add(new GttVoidType());
	}
	
	protected Resource[] getSchemaResources() {
		Resource[] resources = null;
		
		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			resources = resolver.getResources("classpath*:" + gsProperties.getSchemaLocationPattern());			   		
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resources;
	}
	
}
