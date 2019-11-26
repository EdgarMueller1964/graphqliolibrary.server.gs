package com.thinkenterprise.graphqlio.server.gs.keyvaluestore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisProperties {

	@Value("${spring.redis.port}")
	private int redisPort;

	@Value("${spring.redis.host}")
	private String redisHost;

	public int getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

}