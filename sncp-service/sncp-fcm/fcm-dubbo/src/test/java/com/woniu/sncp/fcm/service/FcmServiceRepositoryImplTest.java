package com.woniu.sncp.fcm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.fcm.FcmApplication;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FcmApplication.class)
public class FcmServiceRepositoryImplTest {
	
	protected static final Logger log = LoggerFactory.getLogger(FcmServiceRepositoryImplTest.class);
	
	@Autowired FcmService fcmService;
	
	@Test public void testQueryIdentity(){
		try {
			String identity = fcmService.queryIdentity(180812L);
			log.info("identity is "+identity);
			Assert.assertNotNull(identity);
		} catch (MissingParamsException e) {
			log.error("testQueryFcmTime 缺少参数");
		} catch (PassportNotFoundException e) {
			log.error("testQueryFcmTime 帐号未查询到");
		}
	}
	
	@Test public void testQueryFcmTime() {
		try {
			String identity = fcmService.queryIdentity(180812L);

        	Long queryFcmTime = fcmService.fcmOnlineTime(identity, 10L);
        	System.out.println("test queryFcmTime:"+queryFcmTime);
        	Assert.assertNotNull(queryFcmTime);
	        	
		} catch (MissingParamsException e) {
			log.error("testQueryFcmTime 缺少参数");
		} catch (PassportNotFoundException e) {
			log.error("testQueryFcmTime 帐号未查询到");
		}
	}
	
	@Test public void testQueryUserFcmTotalTime(){
		try {
			String identity = fcmService.queryIdentity(180812L);
			PassportFcmTotalTimeTo userFcmTotalTimeTo = fcmService.queryUserFcmTotalTime(identity, 10L);
			System.out.println("test userFcmTotalTimeTo:"+userFcmTotalTimeTo);
			Assert.assertNotNull(userFcmTotalTimeTo);
		} catch (MissingParamsException e) {
			log.error("testQueryFcmTime 缺少参数");
		} catch (PassportNotFoundException e) {
			log.error("testQueryFcmTime 帐号未查询到");
		}
	}
	
}
