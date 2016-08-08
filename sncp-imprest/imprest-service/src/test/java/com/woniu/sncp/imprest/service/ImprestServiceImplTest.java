package com.woniu.sncp.imprest.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.imprest.ImprestApplication;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImprestApplication.class)
public class ImprestServiceImplTest {
	
	@Autowired private ImprestServiceImpl imprestService;
	
	@Test public void testQueryImprestLogs() throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Long aid = 180812L;
		Long gameId = 10L;
		Long areaId = 7100028L;
		List<Long> platformIds = new ArrayList<Long>();
		Date startDate = sdf.parse("2014-01-01 00:00:00");
		Date endDate = sdf.parse("2016-08-08 00:00:00");
		List<Long> speCards = new ArrayList<Long>();
		
		List<ImprestLogDTO> logs = imprestService.queryImprestLogs(aid, gameId, areaId, platformIds, startDate, endDate, speCards);
		System.out.println(logs.size());
		assertEquals(107, logs.size());
	}
	
	@Test public void testQueryImprestLogsWithPlatforId() throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Long aid = 180812L;
		Long gameId = 10L;
		Long areaId = 7100028L;
		List<Long> platformIds = new ArrayList<Long>();
		platformIds.add(447L);
		Date startDate = sdf.parse("2014-01-01 00:00:00");
		Date endDate = sdf.parse("2016-08-08 00:00:00");
		List<Long> speCards = new ArrayList<Long>();
		
		List<ImprestLogDTO> logs = imprestService.queryImprestLogs(aid, gameId, areaId, platformIds, startDate, endDate, speCards);
		System.out.println(logs.size());
		assertEquals(19, logs.size());
	}
	
	@Test public void testQueryImprestLogsWithSpeCards() throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Long aid = 180812L;
		Long gameId = 10L;
		Long areaId = 7100028L;
		List<Long> platformIds = new ArrayList<Long>();
		platformIds.add(447L);
		Date startDate = sdf.parse("2014-01-01 00:00:00");
		Date endDate = sdf.parse("2016-08-08 00:00:00");
		List<Long> speCards = new ArrayList<Long>();
		speCards.add(12819L);
		
		List<ImprestLogDTO> logs = imprestService.queryImprestLogs(aid, gameId, areaId, platformIds, startDate, endDate, speCards);
		System.out.println(logs.size());
		assertEquals(0, logs.size());
	}
}
