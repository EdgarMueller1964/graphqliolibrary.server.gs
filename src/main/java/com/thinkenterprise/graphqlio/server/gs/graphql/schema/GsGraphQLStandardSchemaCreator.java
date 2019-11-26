package com.thinkenterprise.graphqlio.server.gs.graphql.schema;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import org.springframework.core.io.Resource;



//@Component
public class GsGraphQLStandardSchemaCreator extends GsGraphQLAbstractSchemaCreator {

    @PostConstruct
    public GraphQLSchema create() { //throws IOException { 
    	    	
        TypeDefinitionRegistry typeRegistry = null; 
                	
    	SchemaParser schemaParser = new SchemaParser();
    	File [] files = getFiles();    	
    	if ( files.length == 1) {
			typeRegistry = schemaParser.parse(files[0]);
    	}
    	else {
    		typeRegistry = new TypeDefinitionRegistry();
    	   	for (File file: files) {    		
        		typeRegistry.merge(schemaParser.parse(file));
        	}   		
    	}
        
        RuntimeWiring wiring = buildRuntimeWiring();
        graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return graphQLSchema;
    }
    
    public RuntimeWiring buildRuntimeWiring(){
    	
    	initScalarTypes();
    	Builder builder = newRuntimeWiring();
    	for (GraphQLScalarType scalarType: scalarTypes ) {
    		builder.scalar(scalarType);
    	}
        return builder.build();
    }
        
	protected File[] getFiles() {
		
		File[] files = null;
		try {
			Resource[] resources = getSchemaResources();
			
			files= new File[resources.length];
			
			for (int i = 0; i < resources.length; ++i ) {
				files[i]=resources[i].getFile();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return files;
	}


}
