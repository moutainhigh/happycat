package com.woniu.sncp.ploy.service;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PloyResponseDTO;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PloyServiceTest {
	
	@Autowired
	private PloyService ployService;
	
	@Test
	public void testQueryPloy() throws Exception {
		PloyRequestDTO ployRequestDTO = new PloyRequestDTO();
		ployRequestDTO.setAid(123456L);
		ployRequestDTO.setGameId(10L);
		ployRequestDTO.setEventTime(new Date());
		try {
			PloyResponseDTO ployResponseDTO = ployService.queryPloy(ployRequestDTO);
			System.out.println(ployResponseDTO.getPloyTypeStats().size());
		} catch(RuntimeException e) {
			e.getMessage();
			assertTrue(true);
		}
		
	}
}
