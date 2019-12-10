/*******************************************************************************
 * *
 * **  Design and Development by msg Applied Technology Research
 * **  Copyright (c) 2019-2020 msg systems ag (http://www.msg-systems.com/)
 * **  All Rights Reserved.
 * ** 
 * **  Permission is hereby granted, free of charge, to any person obtaining
 * **  a copy of this software and associated documentation files (the
 * **  "Software"), to deal in the Software without restriction, including
 * **  without limitation the rights to use, copy, modify, merge, publish,
 * **  distribute, sublicense, and/or sell copies of the Software, and to
 * **  permit persons to whom the Software is furnished to do so, subject to
 * **  the following conditions:
 * **
 * **  The above copyright notice and this permission notice shall be included
 * **  in all copies or substantial portions of the Software.
 * **
 * **  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * **  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * **  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * **  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * **  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * **  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * **  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * *
 ******************************************************************************/
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
