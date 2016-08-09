package com.woniu.sncp.vip.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.vip.VipApplication;
import com.woniu.sncp.vip.entity.PassportVip;
import com.woniu.sncp.vip.entity.PassportVipPK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(VipApplication.class)
public class PassportVipRepositoryTest {
	
	@Autowired
	private PassportVipRepository passportVipRepository;

	@Test
	public void testFindById() {
		PassportVipPK passportVipPK = new PassportVipPK();
		passportVipPK.setAid(1700858171L);
		passportVipPK.setGameId(54L);
		PassportVip passportVip = passportVipRepository.findById(passportVipPK);
		assertNotNull(passportVip);
	}

}
