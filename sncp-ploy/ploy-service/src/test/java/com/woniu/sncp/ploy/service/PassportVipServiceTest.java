package com.woniu.sncp.ploy.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.vip.dto.PassportVipDTO;
import com.woniu.sncp.vip.service.PassportVipService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PassportVipServiceTest {

	@Autowired
	private PassportVipService passportVipService;
	
	@Test
	public void testFindPassportVipByAidAndGameId() {
		PassportVipDTO passportVipDTO = passportVipService.findPassportVipByAidAndGameId(1700858171L, 54L);
		assertNotNull(passportVipDTO);
	}
	
	
}
