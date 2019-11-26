package com.thinkenterprise.graphqlio.server.gs.keyvaluestore;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import redis.embedded.RedisServer;

@Service
@Scope("singleton")
public class GsGraphQLRedisService {

	final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	@Autowired
	private RedisProperties redisProperties;

	private RedisServer embeddedRedisServer;

	public void start() throws IOException {
		log.info("Starting embedded RedisServer.");

		// this.embeddedRedisServer = new RedisServer(redisProperties.getRedisPort());

		this.embeddedRedisServer = RedisServer.builder().port(redisProperties.getRedisPort())
				// .redisExecProvider(customRedisExec) //com.github.kstyrc (not
				// com.orange.redis-embedded)
				.setting("maxmemory 128M") // maxheap 128M
				.build();

		this.embeddedRedisServer.start();
		log.info("Started embedded RedisServer.");
	}

	public void stop() {
		log.info("Stopping embedded RedisServer.");
		this.embeddedRedisServer.stop();
		log.info("Stopped embedded RedisServer.");
	}

}
