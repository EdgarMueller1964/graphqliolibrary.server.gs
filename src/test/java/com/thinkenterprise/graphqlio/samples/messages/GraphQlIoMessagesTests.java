/*
**  Design and Development by msg Applied Technology Research
**  Copyright (c) 2019-2020 msg systems ag (http://www.msg-systems.com/)
**  All Rights Reserved.
** 
**  Permission is hereby granted, free of charge, to any person obtaining
**  a copy of this software and associated documentation files (the
**  "Software"), to deal in the Software without restriction, including
**  without limitation the rights to use, copy, modify, merge, publish,
**  distribute, sublicense, and/or sell copies of the Software, and to
**  permit persons to whom the Software is furnished to do so, subject to
**  the following conditions:
**
**  The above copyright notice and this permission notice shall be included
**  in all copies or substantial portions of the Software.
**
**  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
**  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
**  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
**  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
**  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
**  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
**  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.thinkenterprise.graphqlio.samples.messages;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.thinkenterprise.graphqlio.samples.Route;
import com.thinkenterprise.graphqlio.samples.QueryResolver;
import com.thinkenterprise.graphqlio.server.gs.handler.GsWebSocketHandler;
import com.thinkenterprise.graphqlio.server.gs.server.GsServer;
import com.thinkenterprise.graphqlio.server.gts.keyvaluestore.GtsGraphQLRedisService;

/**
 * Class used to process any incoming message sent by clients via WebSocket
 * supports subprotocols (CBOR, MsgPack, Text)
 * triggers process to indicate outdating queries and notifies clients
 *
 * @author Michael Schäfer
 * @author Torsten Kühnert
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GraphQlIoMessagesTests {

	@LocalServerPort
	private int port;

	@Autowired
	private GsServer graphqlioServer;

	@Autowired
	private GtsGraphQLRedisService redisService;

	@Autowired
	private QueryResolver routeResolver;

	@BeforeAll
	private void startServers() throws IOException {
		// 1st redis:
		this.redisService.start();
		// 2nd io:
		this.graphqlioServer.start();
	}

	@AfterAll
	private void stopServers() {
		this.graphqlioServer.stop();
		this.redisService.stop();
	}

	@BeforeEach
	private void initRoutes() {
		this.routeResolver.init();
	}

	private final String simpleQuery = "[1,0,\"GRAPHQL-REQUEST\",query { routes { flightNumber departure destination } } ]";

	@Test
	void textAnswer() {
		try {
			GraphQlIoMessagesTestsHandler webSocketHandler = new GraphQlIoMessagesTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
			headers.setSecWebSocketProtocol(Arrays.asList(GsWebSocketHandler.SUB_PROTOCOL_TEXT));

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage textMessage = new TextMessage(simpleQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(textMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 1 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 1);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 0);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"routes":[{"flightNumber":"LH2122","departure":"MUC","destination":"BRE"},{"flightNumber":"LH2084","departure":"CGN","destination":"BER"}]}}]

			String flight_a = "{\"flightNumber\":\"LH2084\",\"departure\":\"CGN\",\"destination\":\"BER\"}";
			String flight_b = "{\"flightNumber\":\"LH2122\",\"departure\":\"MUC\",\"destination\":\"BRE\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_a)));
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_b)));

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private final String mutationQuery = "[1,0,\"GRAPHQL-REQUEST\",mutation { updateRoute( flightNumber: \"LH2084\" input: { flightNumber: \"LH2084\" departure: \"HAM\" destination: \"MUC\" disabled: false } ) { flightNumber departure destination } } ]";

	@Test
	void cborAnswer() {
		try {
			GraphQlIoMessagesTestsHandler webSocketHandler = new GraphQlIoMessagesTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
			headers.setSecWebSocketProtocol(Arrays.asList(GsWebSocketHandler.SUB_PROTOCOL_CBOR));

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage textMessage = GsWebSocketHandler.createFromStringCbor(mutationQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(textMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 1 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 1);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 0);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"updateRoute":{"flightNumber":"LH2084","departure":"HAM","destination":"MUC"}}}]

			String flight_a = "{\"flightNumber\":\"LH2084\",\"departure\":\"HAM\",\"destination\":\"MUC\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_a)));

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
