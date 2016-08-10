package com.woniu.sncp.vip.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.vip.VipApplication;
import com.woniu.sncp.vip.dto.PassportVipDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(VipApplication.class)
public class PassportVipServiceImplTest {
	
	@Autowired
	private PassportVipService passportVipService;

	@Test
	public void testFindPassportVipByAidAndGameId() {
		PassportVipDTO passportVipDTO = passportVipService.findPassportVipByAidAndGameId(1700858171L, 54L);
		assertNotNull(passportVipDTO);
	}

}
