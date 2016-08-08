package com.woniu.sncp.imprest.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.imprest.ImprestApplication;
import com.woniu.sncp.imprest.entity.ImprestOrder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImprestApplication.class)
public class ImprestOrderRepositoryTest {
	
	@Autowired
	private ImprestOrderRepository imprestOrderRepository;

	@Test
	public void testGetOne() {
		ImprestOrder imprestOrder = imprestOrderRepository.findOne(1142L);
		assertNotNull(imprestOrder);
	}

}
