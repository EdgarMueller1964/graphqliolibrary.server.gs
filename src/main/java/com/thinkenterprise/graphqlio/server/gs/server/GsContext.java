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
package com.thinkenterprise.graphqlio.server.gs.server;

import org.springframework.web.socket.WebSocketSession;

import com.thinkenterprise.graphqlio.server.gts.context.GtsContext;
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
	
	public GtsContext toGtsContext( ) {
		return GtsContext.builder()
				.webSocketSession(webSocketSession)
				.scope(scope)
				.graphQLSchema(graphQLSchema)
				.build();
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
