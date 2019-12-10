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
