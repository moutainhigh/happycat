package com.woniu.sncp.fcm.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.fcm.FcmApplication;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FcmApplication.class)
public class FcmGameProfileRepositoryImplTest {

	@Autowired FcmGameProfileServiceRepositoryImpl service;
	
	public @Test void testSave(){
		FcmGameProfileTo to = new FcmGameProfileTo();
		
		service.delete(-123456L, 10L);
		
		to.setAoId(-123456L);
		to.setGameId(10L);
		service.save(to);
		
		FcmGameProfileTo query = service.query(-123456L, 10L);
		Assert.assertNotNull(query);
		
		List<FcmGameProfileTo> query2 = service.query(-123456L);
		Assert.assertEquals(true, query2.stream().anyMatch(p->p.getAoId() == -123456L));
		
		service.delete(-123456L, 10L);
		
		query = service.query(-123456L, 10L);
		Assert.assertNull(query);
	}
	
	public @Test void testQuery(){
		List<FcmGameProfileTo> query = service.query(-123456L);
		Assert.assertNotNull(query);
	}
	
	public @Test void testBatchSave(){
		FcmGameProfileTo to = new FcmGameProfileTo();
		to.setAoId(7L);
		to.setGameId(10L);
		service.save(to);
	}
}
