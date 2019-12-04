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
//					throw new GsException();
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
