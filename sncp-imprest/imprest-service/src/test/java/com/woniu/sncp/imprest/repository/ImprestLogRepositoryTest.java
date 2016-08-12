package com.woniu.sncp.imprest.repository;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.imprest.ImprestApplication;
import com.woniu.sncp.imprest.entity.ImprestLog;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImprestApplication.class)
public class ImprestLogRepositoryTest {
	
	@Autowired
	private ImprestLogRepository imprestLogRepository;

	@Test
	public void testGetOne() {
		ImprestLog imprestLog = imprestLogRepository.findOne(21969L);
		assertNotNull(imprestLog);
	}
	
	@Test
	public void testFindSumAmountAndPriceByGameIdAndAidAndImprestDate() {
		Calendar start = Calendar.getInstance();
		start.set(2016, 7, 12, 00,00,00);
		Calendar end = Calendar.getInstance();
		end.set(2016, 7, 12, 12,00,00);
		Long sumMoney = imprestLogRepository.findSumAmountAndPriceByGameIdAndAidAndImprestDate(84L, 1436708L, start.getTime(), end.getTime());
		assertTrue(sumMoney > 0);
	}

}
