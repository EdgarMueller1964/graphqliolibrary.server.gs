package com.thinkenterprise.graphqlio.server.gs.graphql;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkenterprise.graphqlio.server.gs.exception.GsException;
import com.thinkenterprise.graphqlio.server.gs.execution.GsExecutionStrategy;
import com.thinkenterprise.graphqlio.server.gs.server.GsContext;
import com.thinkenterprise.graphqlio.server.wsf.domain.WsfFrame;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

public class GsGraphQLExecution implements GsExecutionStrategy {

	
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void execute(GsContext graphQLIOContext) {

		// @Fixme : Execution Input support some other parameters like Root Object,
		// Operation Name, Variable etc.
		// ExecutionResult executionResult = graphQL.execute(new ExecutionInput(query, operationName, context, rootObject, transformVariables(schema, query, variables)));
	
		String result;

		// Create Engine 
		GraphQL graphQL = GraphQL.newGraphQL(graphQLIOContext.getGraphQLSchema()).build();
		
		// Build Execution Input from our GraphQL IO Context 
		ExecutionInput executionInput = ExecutionInput.newExecutionInput()
				.query(graphQLIOContext.getRequestMessage().getData()).context(graphQLIOContext).build();

		// Execute 
		ExecutionResult executionResult = graphQL.execute(executionInput);

		// Convert Result in JSON 
		try {
			result = objectMapper.writeValueAsString(executionResult.toSpecification());
		} catch (JsonProcessingException e) {
			throw new GsException();
		}
	
		// Build Response Message from Request Message an Result 
		WsfFrame responseMessage = WsfFrame.builder()
														   .fromRequestMessage(graphQLIOContext.getRequestMessage())
														   .data(result)
														   .build();
													   
		graphQLIOContext.setResponseMessage(responseMessage);
			
	}
	
	
	
}
