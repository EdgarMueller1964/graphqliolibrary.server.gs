package com.thinkenterprise.graphqlio.server.gs.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thinkenterprise.graphqlio.server.gs.graphql.GsGraphQLService;

@Service
public class GsServer {

	@Autowired
	private GsGraphQLService gsGraphQLService;

	
	public boolean start() {
		return gsGraphQLService.start();
	}
	
	public void stop() {
		gsGraphQLService.stop();
	}
	
}
