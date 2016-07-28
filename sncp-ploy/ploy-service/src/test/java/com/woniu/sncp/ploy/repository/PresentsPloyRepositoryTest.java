package com.woniu.sncp.ploy.repository;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.entity.PresentsPloy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PresentsPloyRepositoryTest {
	
	@Autowired
	private PresentsPloyRepository presentsPloyRepository;

	@Test
	public void testFindByLimitGameAndState() {
		List<PresentsPloy> presentsPloys = presentsPloyRepository.findByLimitGameAndState("10", new Date());
		System.out.println(presentsPloys.size());
		assertNotNull(presentsPloys);
	}

}
