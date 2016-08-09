package com.woniu.sncp.vip.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.vip.VipApplication;
import com.woniu.sncp.vip.entity.PassportVipPresents;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(VipApplication.class)
public class PassportVipPresentsRepositoryTest {

	@Autowired
	private PassportVipPresentsRepository passportVipPresentsRepository;

	@Test
	public void testFindBySendLevel() {
		Page<PassportVipPresents> page = passportVipPresentsRepository.findBySendLevel("2", new PageRequest(0, 1));
		assertNotNull(page.getContent().get(0));
	}

}
