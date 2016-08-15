package com.woniu.sncp.imprest.repository;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
		BigDecimal sumMoney = imprestLogRepository.findSumAmountAndPriceByGameIdAndAidAndImprestDate(84L, 1436708L, start.getTime(), end.getTime());
		assertTrue(sumMoney.longValue() > 0);
	}
	
	@Test
	public void testFindSumAmountByAidAndGameIdAndGAreaidAndImprestDate() {
		Calendar start = Calendar.getInstance();
		start.set(2012, 8, 4, 00,00,00);
		Calendar end = Calendar.getInstance();
		end.set(2016, 7, 6, 00,00,00);
		List<Long> gAreaIds = new ArrayList<Long>();
		gAreaIds.add(7120001L);
		gAreaIds.add(7120005L);
		gAreaIds.add(7120002L);
		Object result = imprestLogRepository.findSumAmountByAidAndGameIdAndGameAreaIdAndImprestDate(180812L, 12L, gAreaIds, "A", start.getTime(), end.getTime());
		assertNotNull(result);
	}
	

}
