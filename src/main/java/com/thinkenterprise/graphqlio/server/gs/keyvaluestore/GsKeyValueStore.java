package com.thinkenterprise.graphqlio.server.gs.keyvaluestore;

import java.util.Set;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class GsKeyValueStore {

	private RedisTemplate<String, String> redisTemplate;
	
	public GsKeyValueStore( RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	public RedisTemplate<String, String> getRedisTemplate() {return redisTemplate; }
	
	/// key pattern "cid:*:sid:*
	/// value: string, e.g. serialized data structure

	private String generateKey(String connectionId, String scopeId) {
		return "cid:" + connectionId+ ":sid:" +scopeId;
	}
	
	public void store(String connectionId, String scopeId, String value ) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(generateKey(connectionId, scopeId), value);		
	}

	public String get (String connectionId, String scopeId) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(generateKey(connectionId, scopeId));		
	}
	
	public boolean delete (String connectionId, String scopeId) {
    	return redisTemplate.delete(generateKey(connectionId, scopeId));
	}

	public boolean hasKey (String connectionId, String scopeId) {
    	return redisTemplate.hasKey(generateKey(connectionId, scopeId));
	}

	public Set<String> getAllKeys()  {
    	return redisTemplate.keys("cid:*:sid:*");
	}
	
	public Set<String> getAllKeysForConnection(String connectionId)  {
		String keyPattern = "cid:" + connectionId + ":sid:*";
    	return redisTemplate.keys(keyPattern);
	}
	
	public Set<String> getAllKeysForScope(String scopeId)  {
		String keyPattern = "cid:*:sid:" + scopeId;
    	return redisTemplate.keys(keyPattern);
	}
	
	
	public void putRecords(String connectionId, String scopeId, String[] records) {
		String value = String.join("@", records);
		store(connectionId, scopeId, value);		
	}

	public String[] getRecords(String connectionId, String scopeId) {
		String value = get(connectionId, scopeId);
		if ( value == null)
			return null;
		return value.split("\\@");
	}
	
	public static String getConnectionId(String key) {
		int indexCid =  key.indexOf("cid:") + 4;
		int indexSid = key.indexOf(":sid:");
		assert (indexCid == 4 &&  indexSid > 4);
		
		if (indexCid == 4 &&  indexSid > 4) {
			return key.substring(indexCid, indexSid);
		}
		else return null;
	}

	public static String getScopeId(String key) {
		int indexCid =  key.indexOf("cid:") + 4;
		int indexSid = key.indexOf(":sid:") + 5;
		assert (indexCid == 4 &&  indexSid >= 10);
		
		if (indexCid == 4 &&  indexSid >= 10) {
			return key.substring(indexSid);
		}
		else return null;
	}

}
