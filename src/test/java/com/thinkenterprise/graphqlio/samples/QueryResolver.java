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

package com.thinkenterprise.graphqlio.samples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.thinkenterprise.graphqlio.server.gts.context.GtsContext;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsRecord;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsRecord.GtsArityType;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsRecord.GtsOperationType;
import com.thinkenterprise.graphqlio.server.gts.tracking.GtsScope;

import graphql.schema.DataFetchingEnvironment;

/**
 * Class used to process any incoming message sent by clients via WebSocket
 * supports subprotocols (CBOR, MsgPack, Text)
 * triggers process to indicate outdating queries and notifies clients
 *
 * @author Michael Schäfer
 * @author Torsten Kühnert
 */

@Component
public class QueryResolver implements GraphQLQueryResolver {

	public Map<String, Route> allRoutes = new HashMap<String, Route>();

	public QueryResolver() {
		this.init();
	}

	public void init() {
		this.allRoutes = new HashMap<String, Route>();
		this.allRoutes.put("LH2084", new Route("LH2084", "CGN", "BER"));
		this.allRoutes.put("LH2122", new Route("LH2122", "MUC", "BRE"));
	}

	public Collection<Route> routes(DataFetchingEnvironment env) {

		Collection<Route> routes = new ArrayList<Route>(this.allRoutes.values());

		List<String> dstIds = new ArrayList<>();
		if (!routes.isEmpty()) {
			routes.forEach(route -> dstIds.add(route.getFlightNumber().toString()));
		} else
			dstIds.add("*");
		GtsContext context = env.getContext();
		GtsScope scope = context.getScope();
		scope.addRecord(
				GtsRecord.builder().op(GtsOperationType.READ).arity(GtsArityType.ALL).dstType(Route.class.getName())
						.dstIds(dstIds.toArray(new String[dstIds.size()])).dstAttrs(new String[] { "*" }).build());

		return routes;
	}

}
