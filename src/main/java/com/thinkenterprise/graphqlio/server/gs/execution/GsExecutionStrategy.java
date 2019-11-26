package com.thinkenterprise.graphqlio.server.gs.execution;

import com.thinkenterprise.graphqlio.server.gs.server.GsContext;

public interface GsExecutionStrategy {
	
	void execute(GsContext graphQLIOContext); 

}
