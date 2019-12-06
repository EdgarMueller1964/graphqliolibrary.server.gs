package com.thinkenterprise.graphqlio.samples.messages;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.thinkenterprise.graphqlio.samples.Route;
import com.thinkenterprise.graphqlio.server.gs.handler.GsWebSocketHandler;

public class GraphQlIoMessagesTestsHandler extends AbstractWebSocketHandler {

	public int text_count = 0;
	public int cbor_count = 0;
	public int msgpack_count = 0;
	public int default_count = 0;

	public int count = 0;

	public List<Route> routes = new ArrayList<Route>();

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (GsWebSocketHandler.SUB_PROTOCOL_TEXT.equalsIgnoreCase(session.getAcceptedProtocol())) {
			this.text_count++;
			this.count++;
			String msg = ((TextMessage) message).getPayload();
			this.addFlights(msg);

		} else if (GsWebSocketHandler.SUB_PROTOCOL_CBOR.equalsIgnoreCase(session.getAcceptedProtocol())) {
			this.cbor_count++;
			this.count++;
			String msg = GsWebSocketHandler.getFromCbor((BinaryMessage) message);
			this.addFlights(msg);

		} else if (GsWebSocketHandler.SUB_PROTOCOL_MSGPACK.equalsIgnoreCase(session.getAcceptedProtocol())) {
			this.msgpack_count++;
			this.count++;
			String msg = GsWebSocketHandler.getFromMsgPack((BinaryMessage) message);
			this.addFlights(msg);

		} else {
			this.default_count++;
			this.count++;
			String msg = ((TextMessage) message).getPayload();
			this.addFlights(msg);
		}
	}

	// [1,1,"GRAPHQL-RESPONSE",{"data":{"routes":[{"flightNumber":"LH2122","departure":"MUC","destination":"BRE"},{"flightNumber":"LH2084","departure":"CGN","destination":"BER"}]}}]
	// [1,1,"GRAPHQL-RESPONSE",{"data":{"updateRoute":{"flightNumber":"LH2084","departure":"HAM","destination":"MUC"}}}]

	private void addFlights(String msg) throws JSONException {
		int pos = msg.indexOf("{\"data\":");
		if (pos > 0) {
			String jsonStr = msg.substring(pos);
			JSONObject jsonObj = new JSONObject(jsonStr);
			JSONObject dataObj = jsonObj.getJSONObject("data");

			if (dataObj.has("routes")) {
				JSONArray routesArr = dataObj.getJSONArray("routes");

				for (int i = 0; i < routesArr.length(); i++) {
					JSONObject flightObj = routesArr.getJSONObject(i);

					this.routes.add(new Route(flightObj.toString()));
				}

			} else if (dataObj.has("updateRoute")) {
				JSONObject flightObj = dataObj.getJSONObject("updateRoute");

				this.routes.add(new Route(flightObj.toString()));
			}
		}
	}

}
