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
