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

package com.thinkenterprise.graphqlio.samples.subscription;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

import com.thinkenterprise.graphqlio.samples.QueryResolver;
import com.thinkenterprise.graphqlio.samples.Route;
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
class GraphQlIoSubscriptionTests {

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

	private List<String> subscriptionIds = new ArrayList<String>();

	private final String subscriptionQuery = "[1,0,\"GRAPHQL-REQUEST\",query { _Subscription { subscribe } routes { flightNumber departure destination disabled signature bookingDate } } ]";
	private final String mutationQuery1a = "[1,0,\"GRAPHQL-REQUEST\",mutation { updateRoute(flightNumber: \"LH2084\" input: { flightNumber: \"LH2084\" departure: \"HAM\" destination: \"ROM\" disabled: true signature: null } ) { flightNumber departure destination disabled signature bookingDate } } ]";
	private final String mutationQuery1b = "[1,0,\"GRAPHQL-REQUEST\",mutation { updateRoute(flightNumber: \"LH2122\" input: { flightNumber: \"LH2122\" departure: \"FRA\" destination: \"BCN\" disabled: true signature: null } ) { flightNumber departure destination disabled signature bookingDate } } ]";
	private final String unsubscribeQuery = "[1,0,\"GRAPHQL-REQUEST\",mutation { _Subscription { unsubscribe( sid: \"%s\" ) } } ]";
	private final String mutationQuery2 = "[1,0,\"GRAPHQL-REQUEST\",mutation { updateRoute(flightNumber: \"LH2084\" input: { flightNumber: \"LH2084\" departure: \"ROM\" destination: \"HAM\" disabled: false } ) { flightNumber departure destination disabled signature bookingDate } } ]";

	@Test
	void textAnswer() {
		try {
			GraphQlIoSubscriptionTestsHandler webSocketHandler = new GraphQlIoSubscriptionTestsHandler(subscriptionIds);

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();

			//
			// 1st: subscriptionQuery
			//
			AbstractWebSocketMessage textMessage = new TextMessage(subscriptionQuery);
			webSocketSession.sendMessage(textMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 1 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 1);
			Assert.assertTrue(subscriptionIds.size() == 1);
			Assert.assertTrue(webSocketHandler.notifier_count == 0);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"_Subscription":{"subscribe":"6d10dd3b-58ff-43de-a29a-5415a4a8f0a5"},"routes":[{"flightNumber":"LH2122","departure":"MUC","destination":"BRE","disabled":null,"signature":null,"bookingDate":null},{"flightNumber":"LH2084","departure":"CGN","destination":"BER","disabled":null,"signature":null,"bookingDate":null}]}}]

			String flight_a = "{\"flightNumber\":\"LH2084\",\"departure\":\"CGN\",\"destination\":\"BER\"}";
			String flight_b = "{\"flightNumber\":\"LH2122\",\"departure\":\"MUC\",\"destination\":\"BRE\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_a)));
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_b)));

			//
			// 2nd: mutationQuery1
			//
			textMessage = new TextMessage(mutationQuery1a);
			webSocketSession.sendMessage(textMessage);
			textMessage = new TextMessage(mutationQuery1b);
			webSocketSession.sendMessage(textMessage);

			start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 5 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 5);
			Assert.assertTrue(subscriptionIds.size() == 1);
			Assert.assertTrue(webSocketHandler.notifier_count == 2);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"updateRoute":{"flightNumber":"LH2084","departure":"HAM","destination":"ROM","disabled":true,"signature":null,"bookingDate":null}}}]
			// [1,0,"GRAPHQL-NOTIFIER","data":[6d10dd3b-58ff-43de-a29a-5415a4a8f0a5]]

			String flight_c1 = "{\"flightNumber\":\"LH2084\",\"departure\":\"HAM\",\"destination\":\"ROM\"}";
			String flight_c2 = "{\"flightNumber\":\"LH2122\",\"departure\":\"FRA\",\"destination\":\"BCN\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_c1)));
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_c2)));
			Assert.assertTrue(routeResolver.allRoutes.values().contains(new Route(flight_c1)));
			Assert.assertTrue(routeResolver.allRoutes.values().contains(new Route(flight_c2)));

			//
			// 3rd: unsubscribeQuery
			//
			textMessage = new TextMessage(unsubscribeQuery.replace("%s", subscriptionIds.get(0)));
			webSocketSession.sendMessage(textMessage);

			start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 6 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 6);
			Assert.assertTrue(subscriptionIds.size() == 1);
			Assert.assertTrue(webSocketHandler.notifier_count == 2);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"updateRoute":{"flightNumber":"LH2084","departure":"HAM","destination":"ROM","disabled":true,"signature":null,"bookingDate":null}}}]
			// [1,0,"GRAPHQL-NOTIFIER","data":[6d10dd3b-58ff-43de-a29a-5415a4a8f0a5]]

			String flight_d = "{\"flightNumber\":\"LH2084\",\"departure\":\"HAM\",\"destination\":\"ROM\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_d)));
			Assert.assertTrue(routeResolver.allRoutes.values().contains(new Route(flight_d)));

			//
			// 4th: mutationQuery2
			//
			textMessage = new TextMessage(mutationQuery2);
			webSocketSession.sendMessage(textMessage);

			start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 7 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 7);
			Assert.assertTrue(subscriptionIds.size() == 1);
			Assert.assertTrue(webSocketHandler.notifier_count == 2);

			// [1,1,"GRAPHQL-RESPONSE",{"data":{"updateRoute":{"flightNumber":"LH2084","departure":"HAM","destination":"ROM","disabled":true,"signature":null,"bookingDate":null}}}]

			String flight_e = "{\"flightNumber\":\"LH2084\",\"departure\":\"ROM\",\"destination\":\"HAM\"}";
			Assert.assertTrue(webSocketHandler.routes.contains(new Route(flight_e)));
			Assert.assertTrue(routeResolver.allRoutes.values().contains(new Route(flight_e)));

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
