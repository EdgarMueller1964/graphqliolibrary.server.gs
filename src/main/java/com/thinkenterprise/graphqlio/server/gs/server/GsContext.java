package com.thinkenterprise.graphqlio.server.gs.server;

import org.springframework.web.socket.WebSocketSession;

import com.thinkenterprise.graphqlio.server.gts.tracking.GtsScope;
import com.thinkenterprise.graphqlio.server.wsf.domain.WsfFrame;

import graphql.schema.GraphQLSchema;

public class GsContext {

	private WebSocketSession webSocketSession;
	private GraphQLSchema graphQLSchema;
	private WsfFrame requestMessage;
	private WsfFrame responseMessage;
	private GtsScope scope;

	private GsContext(Builder builder) {
		this.webSocketSession=builder.webSocketSession;
		this.graphQLSchema=builder.graphQLSchema;
		this.requestMessage=builder.requestMessage;
		this.responseMessage=builder.responseMessage;
		this.scope=builder.scope;
	}

	public GtsScope getScope() {
		return this.scope;
	}

	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}

	public GraphQLSchema getGraphQLSchema() {
		return this.graphQLSchema;
	}

	public WsfFrame getResponseMessage() {
		return this.responseMessage;
	}

	public void setResponseMessage(WsfFrame responseMessage) {
		this.responseMessage = responseMessage;
	}

	public WsfFrame getRequestMessage() {
		return this.requestMessage;
	}

	public void setRequestMessage(WsfFrame requestMessage) {
		this.requestMessage = requestMessage;
	}

	public static Builder builder() {
		return new Builder();
	} 

	public static final class Builder {

		private WebSocketSession webSocketSession;
		private GraphQLSchema graphQLSchema;
		private WsfFrame requestMessage;
		private WsfFrame responseMessage;
		private GtsScope scope;

		private Builder() {

		}

		public Builder webSocketSession(WebSocketSession webSocketSession) {
			this.webSocketSession = webSocketSession;
			return this;
		}

		public Builder graphQLSchema(GraphQLSchema graphQLSchema) {
			this.graphQLSchema = graphQLSchema;
			return this;
		}

		public Builder requestMessage(WsfFrame requestMessage) {
			this.requestMessage = requestMessage;
			return this;
		}

		public Builder responseMessage(WsfFrame responseMessage) {
			this.responseMessage = responseMessage;
			return this;
		}

		public Builder scope(GtsScope scope) {
			this.scope=scope;
			return this;
		}

		public GsContext build() {
			return new GsContext(this);
		}

	}

}
