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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
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
public class MutationResolver implements GraphQLMutationResolver {

	@Autowired
	private QueryResolver routeResolver;

	public Route updateRoute(String flightNumber, Route input, DataFetchingEnvironment env) {

		Route route = routeResolver.allRoutes.get(flightNumber);

		route.setFlightNumber(input.getFlightNumber());
		route.setDeparture(input.getDeparture());
		route.setDestination(input.getDestination());
		route.setDisabled(input.getDisabled());

		GtsContext context = env.getContext();
		GtsScope scope = context.getScope();
		scope.addRecord(GtsRecord.builder().op(GtsOperationType.UPDATE).arity(GtsArityType.ONE)
				.dstType(Route.class.getName()).dstIds(new String[] { route.getFlightNumber().toString() })
				.dstAttrs(new String[] { "*" }).build());

		return route;
	}

}
