package com.woniu.sncp.fcm.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.fcm.FcmApplication;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FcmApplication.class)
public class FcmServiceRepositoryImplTest {
	
	@Autowired FcmService fcmService;
	
	@Test public void testQueryFcmTime() {
        for (int i = 0; i < 10; i++) {
        	Long queryFcmTime = fcmService.fcmOnlineTime(123L, 10L);
        	System.out.println("test queryFcmTime:"+queryFcmTime);
        	Assert.assertNotNull(queryFcmTime);
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}    	
	}
	
	@Test public void testQueryUserFcmTotalTime(){
		PassportFcmTotalTimeTo userFcmTotalTimeTo = fcmService.queryUserFcmTotalTime(123L, 10L);
		System.out.println("test userFcmTotalTimeTo:"+userFcmTotalTimeTo);
		Assert.assertNotNull(userFcmTotalTimeTo);
	}
	
}
