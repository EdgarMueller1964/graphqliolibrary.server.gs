package com.thinkenterprise.graphqlio.server.gs.actuator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounter;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNames;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNotification;

@Component
public class GsCounter implements GtsCounter {
	
	private List<GtsCounterNotification> counterNotificationList = new ArrayList<>();;
	private GtsCounterNames[] counters = 
		{ GtsCounterNames.CONNECTIONS, GtsCounterNames.SCOPES, GtsCounterNames.RECORDS};
	

	@Override
	public GtsCounterNames[] getCounters() {
		return counters;
	}
	
	@Override
	public void registerCounterNotification( GtsCounterNotification counterNotification) {
		counterNotificationList.add(counterNotification);
	}
	
	@Override
	public void modifyCounter(GtsCounterNames name, long byNumber) {
		counterNotificationList.forEach(counterNotification -> counterNotification.onModifiedCounter(name, byNumber));		
	}

}
