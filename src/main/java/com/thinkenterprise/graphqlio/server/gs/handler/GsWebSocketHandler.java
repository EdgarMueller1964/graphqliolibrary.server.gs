package com.thinkenterprise.graphqlio.server.gs.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkenterprise.graphqlio.server.gs.execution.GsExecutionStrategy;
import com.thinkenterprise.graphqlio.server.gs.graphql.schema.GsGraphQLSchemaCreator;
import com.thinkenterprise.graphqlio.server.gs.server.GsContext;
import com.thinkenterprise.graphqlio.server.gts.evaluation.GtsEvaluation;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsConnection;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsScope;
import com.thinkenterprise.graphqlio.server.wsf.converter.WsfConverter;
import com.thinkenterprise.graphqlio.server.wsf.domain.WsfFrame;
import com.thinkenterprise.graphqlio.server.wsf.domain.WsfFrameType;
import com.thinkenterprise.graphqlio.server.wsf.event.WsfInboundFrameEvent;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;

@Component
public class GsWebSocketHandler extends AbstractWebSocketHandler implements ApplicationListener<WsfInboundFrameEvent>, SubProtocolCapable {

	public static final String SUB_PROTOCOL_TEXT = "text";
	public static final String SUB_PROTOCOL_CBOR = "cbor";
	public static final String SUB_PROTOCOL_MSGPACK = "msgpack";

	private final Logger logger = LoggerFactory.getLogger(GsWebSocketHandler.class);

	private final Map<String, GtsConnection> webSocketConnections = new ConcurrentHashMap<>();
	private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();

	private final WsfConverter requestConverter;

	private final WsfConverter responseConverter;

	private final WsfConverter notifyerConverter;

	private final GsExecutionStrategy graphQLIOQueryExecution;

	private final GtsEvaluation graphQLIOEvaluation;

	private final GsGraphQLSchemaCreator gsGraphQLSchemaCreator;

	@Autowired
	public GsWebSocketHandler(ObjectMapper objectMapper, GsExecutionStrategy executionStrategy,
			GtsEvaluation evaluation, GsGraphQLSchemaCreator schemaCreator) {

		requestConverter = new WsfConverter(objectMapper, WsfFrameType.GRAPHQLREQUEST);
		responseConverter = new WsfConverter(objectMapper, WsfFrameType.GRAPHQLRESPONSE);
		notifyerConverter = new WsfConverter(objectMapper, WsfFrameType.GRAPHQLNOTIFIER);
		graphQLIOQueryExecution = executionStrategy;
		graphQLIOEvaluation = evaluation;
		gsGraphQLSchemaCreator = schemaCreator;
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

		logger.info("GraphQLIO handleMessage received graphqlio message::getAcceptedProtocol = " + session.getAcceptedProtocol());

		if (SUB_PROTOCOL_TEXT.equalsIgnoreCase(session.getAcceptedProtocol())
				&& message instanceof TextMessage) {

			this.handleTextMessage(session, (TextMessage) message);

		} else if (SUB_PROTOCOL_CBOR.equalsIgnoreCase(session.getAcceptedProtocol())
				&& message instanceof BinaryMessage) {

			this.handleCborMessage(session, (BinaryMessage) message);

		} else if (SUB_PROTOCOL_MSGPACK.equalsIgnoreCase(session.getAcceptedProtocol())
				&& message instanceof BinaryMessage) {

			this.handleMsgPackMessage(session, (BinaryMessage) message);

		} else {
			// super.handleMessage(session, message);
			this.handleTextMessage(session, (TextMessage) message);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		logger.info("GraphQLIO handleTextMessage session :" + session);
		logger.info("GraphQLIO handleTextMessage session ID :" + session.getId());
		logger.info("GraphQLIO handleTextMessage this :" + this);
		logger.info("GraphQLIO handleTextMessage Thread :" + Thread.currentThread());

		this.handleStringMessage(session, message.getPayload());
	}

	protected void handleCborMessage(WebSocketSession session, BinaryMessage message) throws Exception {

		logger.info("GraphQLIO handleCborMessage session :" + session);
		logger.info("GraphQLIO handleCborMessage session ID :" + session.getId());
		logger.info("GraphQLIO handleCborMessage this :" + this);
		logger.info("GraphQLIO handleCborMessage Thread :" + Thread.currentThread());

		// vom Client muß geschickt werden, was hier erwartet wird.
		// zunächst wird hier nur ein einzelner ByteString erwartet,
		// der mit einem Query-String im richtigen Format gefüllt ist.

		String input = getFromCbor(message);
		logger.info("cbor.input = " + input);

		if (input != null) {
			this.handleStringMessage(session, input);
		} else {
			logger.info("GraphQLIO handleCborMessage : NO valid CBOR message");
		}
	}

	protected void handleMsgPackMessage(WebSocketSession session, BinaryMessage message) throws Exception {

		logger.info("GraphQLIO handleMsgPackMessage session :" + session);
		logger.info("GraphQLIO handleMsgPackMessage session ID :" + session.getId());
		logger.info("GraphQLIO handleMsgPackMessage this :" + this);
		logger.info("GraphQLIO handleMsgPackMessage Thread :" + Thread.currentThread());

		// vom Client muß geschickt werden, was hier erwartet wird.
		// zunächst wird hier nur ein einzelner String erwartet,
		// der mit einem Query-String im richtigen Format gefüllt ist.

		String input = getFromMsgPack(message);
		logger.info("msgPack.input = " + input);

		this.handleStringMessage(session, input);
	}

	protected void handleStringMessage(WebSocketSession session, String message) throws Exception {

		logger.info("GraphQLIO handle String message = " + message);

		// Convert Frame to Message
		WsfFrame requestMessage = requestConverter.convert(message);
		
		// Get the Connection, create a Scope and push it to the context
		GtsConnection connection = webSocketConnections.get(session.getId());
		GtsScope scope = null;
		/// check if request message is a Subscription message (any of unsubscribe, pause, resume) and retrieve scopeId 
		/// returns valid UUID as String, null otherwise
		
		/// ToDo: check if Scope generation could be delegated to (Gts-)Resolver
		/// and gts library holds GtsConnection map resp. GtsScope list 
		
		String scopeId = this.getSubscriptionScopeId(requestMessage.getData());
		if (scopeId != null) {
			scope = connection.getScopeById(scopeId);
		}
		
		if (scope == null) {
			scope = GtsScope.builder()
										.withQuery(requestMessage.getData())
										.withConnectionId(connection.getConnectionId()).build();
			connection.addScope(scope);			
		}

		// Create Context Information for Execution
		GsContext graphQLIOContext = GsContext.builder().webSocketSession(session)
				.graphQLSchema(gsGraphQLSchemaCreator.getGraphQLSchema()).requestMessage(requestMessage).scope(scope)
				.build();

		// Execute Message
		graphQLIOQueryExecution.execute(graphQLIOContext);

		// Convert Result Message to Frame
		String answerFrame = responseConverter.convert(graphQLIOContext.getResponseMessage());

		// Send back
		sendAnswerBackToClient(session, answerFrame);

		// Evaluate Subscriptions and notify clients
		List<String> sids = graphQLIOEvaluation.evaluateOutdatedSids(graphQLIOContext.getScope());

		sids.forEach(sid -> {
			logger.info("GraphQLIO Scope Evaluation: Scope ("+sid+") outdated");
		});
		
		Map<String, Set<String>> sids4cid = graphQLIOEvaluation.evaluateOutdatedsSidsPerCid(sids,
				webSocketConnections.values());
		
		sendNotifierMessageToClients(sids4cid, requestMessage);
	}

	private void sendAnswerBackToClient(WebSocketSession session, String answerFrame) throws Exception {

		logger.info("GraphQLIO sendAnswerBackToClient::getAcceptedProtocol = " + session.getAcceptedProtocol());

		if (SUB_PROTOCOL_TEXT.equalsIgnoreCase(session.getAcceptedProtocol())) {

			session.sendMessage(new TextMessage(answerFrame));

		} else if (SUB_PROTOCOL_CBOR.equalsIgnoreCase(session.getAcceptedProtocol())) {

			session.sendMessage(createFromStringCbor(answerFrame));

		} else if (SUB_PROTOCOL_MSGPACK.equalsIgnoreCase(session.getAcceptedProtocol())) {

			session.sendMessage(createFromStringMsgPack(answerFrame));

		} else {
			// DEFAULT is TEXT:
			session.sendMessage(new TextMessage(answerFrame));
		}
	}

	public static String getFromCbor(BinaryMessage message) throws CborException {
		List<DataItem> dataItems = CborDecoder.decode(message.getPayload().array());

		// wenn keine Exception:
		if (dataItems == null || dataItems.size() < 1) {
			// logging

		} else if (dataItems.size() >= 1) {
			if (dataItems.size() >= 2) {
				// logging
			}

			DataItem dataItem = dataItems.get(0);
			// logging

			if (dataItem instanceof ByteString) {
				String input = new String(((ByteString) dataItem).getBytes());
				// logging

				return input;

			} else {
				// logging
			}
		}

		// logging
		return null;
	}

	public static String getFromMsgPack(BinaryMessage message) throws IOException {
		MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(message.getPayload().array());

		String input = unpacker.unpackString();
		// logging
		unpacker.close();

		return input;
	}

	public static BinaryMessage createFromStringCbor(String message) throws CborException {
		byte[] bytes = message.getBytes();
		DataItem dataItem = new ByteString(bytes);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new CborEncoder(os).encode(dataItem);

		return new BinaryMessage(os.toByteArray());
	}

	public static BinaryMessage createFromStringMsgPack(String message) throws IOException {
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packString(message);
		packer.close();

		return new BinaryMessage(packer.toByteArray());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		webSocketConnections.put(session.getId(), GtsConnection.builder().fromSession(session).build());
		webSocketSessions.put(session.getId(), session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		GtsConnection connection = webSocketConnections.get(session.getId());
		if (connection != null) {
			/// connection scopes are implicitly garbage collected once connection is not referenced anymore
			graphQLIOEvaluation.onCloseConnection(connection.getConnectionId());
			
		}		
		webSocketConnections.remove(session.getId());
		webSocketSessions.remove(session.getId());
	}

	private void sendNotifierMessageToClients(Map<String, Set<String>> sids4cid, WsfFrame requestMessage)
			throws Exception {

		Set<String> cids = sids4cid.keySet();

		for (String cid : cids) {
			WsfFrame message = WsfFrame.builder().fid(requestMessage.getFid()).rid(requestMessage.getRid())
					.type(WsfFrameType.GRAPHQLNOTIFIER).data(notifyerConverter.createData(sids4cid.get(cid))).build();
			String frame = notifyerConverter.convert(message);
			WebSocketSession sessionForCid = webSocketSessions.get(cid);
			if (sessionForCid != null ) {
				sendAnswerBackToClient(sessionForCid, frame);
			}

		}

	}

	@Override
	public void onApplicationEvent(WsfInboundFrameEvent event) {
		try {
			webSocketSessions.get(event.getCid())
					.sendMessage(new TextMessage(requestConverter.convert(event.getFrame())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/// check if message is a "Subscription - mutation" and contains valid UUID 
	private String getSubscriptionScopeId( String requestMessage ) {
		if (requestMessage.contains("mutation")  &&  requestMessage.contains("_Subscription")) {
			int indexOf = requestMessage.indexOf("sid:");
			if (indexOf > 0) {
				String uuidString = requestMessage.substring(indexOf);
				if ( uuidString.length() > 0) {
					try {
						UUID uuid = UUID.fromString(uuidString);
						return uuidString;
					}
					catch( IllegalArgumentException e) {
						logger.info("GsWebSocketHandler::getSubscriptionScopeId: uuidString (" + uuidString +") does not represent a valid UUID");
					}
					
				}
			}			
		}
		
		return null;
	}

	@Override
	public List<String> getSubProtocols() {
		return Arrays.asList(SUB_PROTOCOL_TEXT, SUB_PROTOCOL_CBOR, SUB_PROTOCOL_MSGPACK);
	}
	
}