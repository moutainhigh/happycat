package com.woniu.sncp.ploy.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.service.ImprestService;
import com.woniu.sncp.ploy.PloyApplication;

import static org.junit.Assert.*;

import org.junit.Test;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class ImprestServiceTest {
	
	@Autowired
	private ImprestService imprestService;
	
	@Test
	public void test() {
		ImprestCardTypeDTO cardTypeDTO = imprestService.findImprestCardById(3043L);
		assertNotNull(cardTypeDTO);
	}
}
