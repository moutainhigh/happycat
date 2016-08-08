package com.woniu.sncp.ploy.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.entity.PloyBusinessLog;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PloyBusinessLogRepositoryTest {
	
	@Autowired
	private PloyBusinessLogRepository ployBusinessLogRepository;

	@Test
	public void testGetOne() {
		PloyBusinessLog businessLog = ployBusinessLogRepository.findOne(5042L);
		assertNotNull(businessLog);
	}

}
