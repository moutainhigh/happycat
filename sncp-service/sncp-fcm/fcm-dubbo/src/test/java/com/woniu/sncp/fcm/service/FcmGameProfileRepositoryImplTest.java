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
	
	@Test
	public void testSave() {
		FcmGameProfileTo to = new FcmGameProfileTo();
		to.setAoId(-125456L);
		to.setGameId(10L);
		service.save(to);
		
		FcmGameProfileTo query = service.query(-125456L, 10L);
		Assert.assertNotNull(query);
		
		List<FcmGameProfileTo> query2 = service.query(-125456L);
		Assert.assertEquals(true, query2.size()>=1);
		
		service.delete(-125456L, 10L);
		
		query = service.query(-125456L, 10L);
		Assert.assertNull(query);
	}
	
}
