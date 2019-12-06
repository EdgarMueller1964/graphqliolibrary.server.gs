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
