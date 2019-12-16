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
package com.thinkenterprise.graphqlio.server.gs.actuator.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounter;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNames;
import com.thinkenterprise.graphqlio.server.gts.actuator.GtsCounterNotification;

/**
 * Class defining custom endpoint for optional information about running graphqlio application 
 *
 * @author Michael Schäfer
 * @author Dr. Edgar Müller
 */


@Endpoint(id="graphqliocounter")
public class GsGraphqlioCounterEndpoint implements GtsCounterNotification {


	private Map<String, Long> counters = new ConcurrentHashMap<>();	
	
	@Autowired
	GtsCounter gtsCounter;
	
	public GsGraphqlioCounterEndpoint( GtsCounter gtsCounter) {
		this.gtsCounter = gtsCounter;
		this.gtsCounter.registerCounterNotification(this);

		
		/// initialize counter
		for (GtsCounterNames counterNames: this.gtsCounter.getCounters()) {
			writeCounter(counterNames.toString(), 0L);
		}
		
	}
	
	@ReadOperation
    public Map<String, Long> counters() {
        return this.counters;
    }	 

	@ReadOperation
    public Long counter(@Selector String name) {
        return counters.get(name);
    }	
	

    @WriteOperation
    public void writeCounter(@Selector String name, Long value) {
        counters.put(name, value);
    }
 
    @DeleteOperation
    public void deleteCounter(@Selector String name) {
        counters.remove(name);
    }

	@Override
	public synchronized void onModifiedCounter(GtsCounterNames counterName, long byNumber) {		
		counters.put(counterName.toString(), (long)counters.get(counterName.toString()).longValue() + byNumber);				
	}	
		
}
