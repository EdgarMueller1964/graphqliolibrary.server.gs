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
package com.thinkenterprise.graphqlio.server.gs.actuator.metrics;

import java.util.concurrent.atomic.AtomicLong;

import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounter;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNames;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNotification;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Class defining custom custom graphqlio gauges counter for actuator metrics endpoint
 *
 * @author Michael Schäfer
 * @author Dr. Edgar Müller
 */
public class GsGraphqlioMeterRegistryCounter implements GtsCounterNotification {

	private MeterRegistry meterRegistry;
	private GtsCounter gtsCounter;
	
	public GsGraphqlioMeterRegistryCounter(MeterRegistry meterRegistry, GtsCounter gtsCounter) {
		this.meterRegistry = meterRegistry;
		this.gtsCounter = gtsCounter;
		initMetricsCounter();
		gtsCounter.registerCounterNotification(this);
	}

	AtomicLong connectionCount = new AtomicLong(0L);
	AtomicLong scopeCount = new AtomicLong(0L);
	AtomicLong recordCount = new AtomicLong(0L);
	
	private void initMetricsCounter() {
		
		Gauge.builder("graphQLIO.connections", connectionCount, AtomicLong::get)   
		        .tag("type", "Connection")
		        .description("The number of Client Connections")
		        .register(meterRegistry);
				
		Gauge.builder("graphQLIO.scopes", scopeCount, AtomicLong::get)    
		        .tag("type", "Scopes")
		        .description("The number of (Query-) Scopes")
		        .register(meterRegistry);

		Gauge.builder("graphQLIO.records", recordCount, AtomicLong::get)   
		        .tag("type", "Record")
		        .description("The number of Scope Records")
		        .register(meterRegistry);

	}
	
	@Override
	public void onModifiedCounter(GtsCounterNames counterName, long byNumber) {
		
		switch (counterName) {
		case CONNECTIONS:
			connectionCount.addAndGet(byNumber);
			break;

		case SCOPES:
			scopeCount.addAndGet(byNumber);
			break;

		case RECORDS:
			recordCount.addAndGet(byNumber);
			break;

		default:
			break;
		}
		
		
		
	}

}
