package com.thinkenterprise.graphqlio.server.gs.graphql.schema;

import java.util.List;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.coxautodev.graphql.tools.SchemaParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;


@Component
public class GsGraphQLSimpleSchemaCreator extends GsGraphQLAbstractSchemaCreator {

	@Autowired(required = false)
	List<GraphQLResolver<?>> resolvers;
	
	@Override
	public GraphQLSchema create() {
		
		
		initScalarTypes();
		GraphQLScalarType[] recScalars = scalarTypes.toArray(new GraphQLScalarType[scalarTypes.size()]);
		
		// @Fixme : Some other parameter like Scalars, should be configured 
		graphQLSchema = SchemaParser.newParser()
		               							.files(getFilePathes())
		               							.scalars(recScalars)
		               						    .resolvers(resolvers)
		               						    .build()
		               						    .makeExecutableSchema();
			          
		
		return graphQLSchema;
		
	}

		
	protected String[] getFilePathes() {
		
		String[] files = null;
				
		Resource[] resources = getSchemaResources();
				
		files= new String[resources.length];
		
		for (int i = 0; i < resources.length; ++i ) {
			files[i]=resources[i].getFilename();
		}
		
		return files;
		
	}
	

}
