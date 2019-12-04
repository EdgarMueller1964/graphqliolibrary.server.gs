package com.thinkenterprise.graphqlio.server.gs.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thinkenterprise.graphqlio.server.gs.graphql.GsGraphQLService;
import com.thinkenterprise.graphqlio.server.gts.keyvaluestore.GtsKeyValueStore;

@Service
public class GsServer {

	@Autowired
	private GsGraphQLService gsGraphQLService;

	@Autowired
	private GtsKeyValueStore gtsKeyValueStore;
	
	
	public boolean start() {
		/// keys associated to a client connection are deleted if connection closes
		/// however there may be keys left from last session if application terminated unexpectedly
		/// therefore we clean up key value store when starting the server
		gtsKeyValueStore.deleteAllKeys();
		return gsGraphQLService.start();
	}
	
	public void stop() {
		gsGraphQLService.stop();
	}
	
}
