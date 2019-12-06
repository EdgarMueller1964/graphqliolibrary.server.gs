package com.thinkenterprise.graphqlio.samples.subprotocols;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import com.thinkenterprise.graphqlio.server.gs.handler.GsWebSocketHandler;
import com.thinkenterprise.graphqlio.server.gs.server.GsServer;
import com.thinkenterprise.graphqlio.server.gts.keyvaluestore.GtsGraphQLRedisService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class GraphQlIoSubprotocolsTests {

	@LocalServerPort
	private int port;

	@Autowired
	private GsServer graphqlioServer;

	@Autowired
	private GtsGraphQLRedisService redisService;

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

	private final String simpleQuery = "[1,0,\"GRAPHQL-REQUEST\",query { _Subscription { subscribe } _Subscription { subscribe } } ]";

	@Test
	void textAnswer() {
		try {
			GraphQlIoSubprotocolsTestsHandler webSocketHandler = new GraphQlIoSubprotocolsTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
			headers.setSecWebSocketProtocol(Arrays.asList(GsWebSocketHandler.SUB_PROTOCOL_TEXT));

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage textMessage = new TextMessage(simpleQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(textMessage);
			webSocketSession.sendMessage(textMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 2 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 2);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 0);

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@Test
	void cborAnswer() {
		try {
			GraphQlIoSubprotocolsTestsHandler webSocketHandler = new GraphQlIoSubprotocolsTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
			headers.setSecWebSocketProtocol(Arrays.asList(GsWebSocketHandler.SUB_PROTOCOL_CBOR));

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage cborMessage = GsWebSocketHandler.createFromStringCbor(simpleQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(cborMessage);
			webSocketSession.sendMessage(cborMessage);
			webSocketSession.sendMessage(cborMessage);
			webSocketSession.sendMessage(cborMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 4 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 4);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 0);

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@Test
	void msgpackAnswer() {
		try {
			GraphQlIoSubprotocolsTestsHandler webSocketHandler = new GraphQlIoSubprotocolsTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
			headers.setSecWebSocketProtocol(Arrays.asList(GsWebSocketHandler.SUB_PROTOCOL_MSGPACK));

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage msgpackMessage = GsWebSocketHandler.createFromStringMsgPack(simpleQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(msgpackMessage);
			webSocketSession.sendMessage(msgpackMessage);
			webSocketSession.sendMessage(msgpackMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 3 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 3);
			Assert.assertTrue(webSocketHandler.default_count == 0);

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@Test
	void defaultAnswer() {
		try {
			GraphQlIoSubprotocolsTestsHandler webSocketHandler = new GraphQlIoSubprotocolsTestsHandler();

			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

			URI uri = URI.create("ws://127.0.0.1:" + port + "/api/data/graph");

			AbstractWebSocketMessage textMessage = new TextMessage(simpleQuery);

			WebSocketClient webSocketClient = new StandardWebSocketClient();
			WebSocketSession webSocketSession = webSocketClient.doHandshake(webSocketHandler, headers, uri).get();
			webSocketSession.sendMessage(textMessage);
			webSocketSession.sendMessage(textMessage);
			webSocketSession.sendMessage(textMessage);
			webSocketSession.sendMessage(textMessage);
			webSocketSession.sendMessage(textMessage);

			long start = System.currentTimeMillis();
			// maximal 1 sec:
			while (webSocketHandler.count < 5 && System.currentTimeMillis() - start < 1000) {
				Thread.sleep(100);
			}

			Assert.assertTrue(webSocketHandler.text_count == 0);
			Assert.assertTrue(webSocketHandler.cbor_count == 0);
			Assert.assertTrue(webSocketHandler.msgpack_count == 0);
			Assert.assertTrue(webSocketHandler.default_count == 5);

			webSocketSession.close();

		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
