package com.thinkenterprise.graphqlio.server.gs.keyvaluestore;

import java.io.IOException;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRedisKVPServiceTest {
	
	@Autowired
	GsKeyValueStore	kvp;

	@Autowired
	GsGraphQLRedisService redisService;
	
	@BeforeEach
	public void startRedis() throws IOException {
		this.redisService.start();
	}

	@AfterEach
	public void stopRedis() {
		this.redisService.stop();
	}
	
	@Test
    public void testKVP() {
		
		Set<String> allKeys = null;
		
		/// reset kvp by deleting all keys
		kvp.getAllKeys().forEach((key) -> kvp.getRedisTemplate().delete(key));
		allKeys = kvp.getAllKeys();
        Assert.assertEquals(0, allKeys.size());        
		
		//// create Keys
		kvp.store("cid1", "sid1", "Value for <cid1,sid1>");
    	allKeys = kvp.getAllKeys();
		kvp.store("cid1", "sid2", "Value for <cid1,sid2>");
    	allKeys = kvp.getAllKeys();
		kvp.store("cid2", "sid3", "Value for <cid2,sid3>");
    	allKeys = kvp.getAllKeys();
		kvp.store("cid2", "sid4", "Value for <cid2,sid4>");
    	allKeys = kvp.getAllKeys();
        
    	Assert.assertTrue(kvp.hasKey("cid1", "sid1"));
     
    	String value = kvp.get("cid1", "sid1");
    	Assert.assertTrue(value.equals("Value for <cid1,sid1>"));
    	
    	allKeys = kvp.getAllKeys();
        Assert.assertEquals(4, allKeys.size());        

    	Set<String> allKeysCid = kvp.getAllKeysForConnection("cid1");
        Assert.assertEquals(2, allKeysCid.size());        

    	Set<String> allKeysSid = kvp.getAllKeysForScope("sid1");
        Assert.assertEquals(1, allKeysSid.size());        

        kvp.delete("cid1", "sid1");
        kvp.delete("cid1", "sid2");
        kvp.delete("cid2", "sid3");
        kvp.delete("cid2", "sid4");
	
	}
	
	
	
}
