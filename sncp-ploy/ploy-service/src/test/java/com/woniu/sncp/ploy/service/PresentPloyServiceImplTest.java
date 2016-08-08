package com.woniu.sncp.ploy.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PresentPloyServiceImplTest {

	@Autowired private PresentPloyService presentPloyService;
	
	@Test public void testQueryJoinedPloyCount(){
		Long ployId = 4637L;
		Long gameId = 10L;
		Long gameAreaId = 7100028L;
		Long userId = 180812L;
		Long impLogId = null;
		Long queryJoinedPloyCount = presentPloyService.queryJoinedPloyCount(ployId, gameId, gameAreaId, userId, impLogId);
		
		System.out.println("imprest query result:"+queryJoinedPloyCount);
	}
	
	@Test public void testQueryJoinedPloyCountNoAreaId(){
		Long ployId = 4637L;
		Long gameId = 10L;
		Long gameAreaId = null;
		Long userId = 180812L;
		Long impLogId = null;
		Long queryJoinedPloyCount = presentPloyService.queryJoinedPloyCount(ployId, gameId, gameAreaId, userId, impLogId);
		
		System.out.println("imprest query result:"+queryJoinedPloyCount);
	}
	
	@Test public void testQueryJoinedPloyCountForEai(){
		Long ployId = 4637L;
		Long gameId = 10L;
		Long gameAreaId = 7100028L;
		Long userId = 180812L;
		Long impLogId = 47232L;
		Long queryJoinedPloyCount = presentPloyService.queryJoinedPloyCount(ployId, gameId, gameAreaId, userId, impLogId);
		
		System.out.println("eai query result :"+queryJoinedPloyCount);
	}
	
	@Test public void testQueryJoinedPloyCountForEaiNoArea(){
		Long ployId = 4637L;
		Long gameId = 10L;
		Long gameAreaId = null;
		Long userId = 180812L;
		Long impLogId = 47232L;
		Long queryJoinedPloyCount = presentPloyService.queryJoinedPloyCount(ployId, gameId, gameAreaId, userId, impLogId);
		
		System.out.println("eai query result :"+queryJoinedPloyCount);
	}
}
