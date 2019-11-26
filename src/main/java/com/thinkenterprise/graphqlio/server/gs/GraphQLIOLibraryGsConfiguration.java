package com.thinkenterprise.graphqlio.server.gs;



import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = GraphQLIOLibraryGsConfiguration.class)
public class GraphQLIOLibraryGsConfiguration {

	
	  private static final Logger logger = LoggerFactory.getLogger(GraphQLIOLibraryGsConfiguration.class);

	  @PostConstruct
	  public void postConstruct(){
	    logger.info("GraphQLIOLIbrary GS Module Loaded!");
	  }
}

