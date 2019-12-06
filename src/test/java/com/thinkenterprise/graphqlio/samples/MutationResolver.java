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
