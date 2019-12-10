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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkenterprise.graphqlio.server.gs.execution.GsExecutionStrategy;
import com.thinkenterprise.graphqlio.server.gs.server.GsContext;
import com.thinkenterprise.graphqlio.server.wsf.domain.WsfFrame;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;

/**
 * Class used to execute any Application Query or Mutation or GraphQL IO Subscription Queries 
 * GsContext is passed to GraphQL DataFetchingEnvironment accessible in resolvers
 *
 * @author Michael Schäfer
 * @author Dr. Edgar Müller
 */
public class GsGraphQLExecution implements GsExecutionStrategy {

	
	private final Logger logger = LoggerFactory.getLogger(GsGraphQLExecution.class);
	
	
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void execute(GsContext graphQLIOContext) {

		// Execution Input support some other parameters like Root Object,
		// Operation Name, Variable etc.
		// ExecutionResult executionResult = graphQL.execute(new ExecutionInput(query, operationName, context, rootObject, transformVariables(schema, query, variables)));
	
		String result = "";

		// Create Engine 
		GraphQL graphQL = GraphQL.newGraphQL(graphQLIOContext.getGraphQLSchema()).build();
		
		// Build Execution Input from our GraphQL IO Context 
		ExecutionInput executionInput = ExecutionInput.newExecutionInput()
				.query(graphQLIOContext.getRequestMessage().getData()).context(graphQLIOContext.toGtsContext()).build();

		try {
			ExecutionResult executionResult = graphQL.execute(executionInput);						
			if ( executionResult != null) {
				
				// Convert Result in JSON 
				try {
					result = objectMapper.writeValueAsString(executionResult.toSpecification());
				} catch (JsonProcessingException e) {
					logger.error(e.toString());
					
					StringBuilder sb = new StringBuilder();
					for (GraphQLError error: executionResult.getErrors()) {
						sb.append(error.toString());
					}
					
					result = sb.append(e.toString()).toString();
				}
			}			
		}
		
		//// GraphQLExceptions are not thrown but are resolved inside graphQL.execute
		catch(Exception e) {
			
			
			logger.error(e.toString());
			result = e.toString();
		}
			
		// Build Response Message from Request Message an Result 
		WsfFrame responseMessage = WsfFrame.builder()
														   .fromRequestMessage(graphQLIOContext.getRequestMessage())
														   .data(result)
														   .build();
													   
		graphQLIOContext.setResponseMessage(responseMessage);
			
	}
		
}
