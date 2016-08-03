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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PloyServiceTest {
	
	@Autowired
	private PloyService ployService;
	
	@Test
	public void testQueryPloy() throws Exception {
		PloyRequestDTO ployRequestDTO = new PloyRequestDTO();
		ployRequestDTO.setGame("10");
		ployRequestDTO.setEventTime(new Date());
		PloyResponseDTO ployResponseDTO = ployService.queryPloy(ployRequestDTO);
		System.out.println(ployResponseDTO.getPloyTypeStats().size());
	}
}
