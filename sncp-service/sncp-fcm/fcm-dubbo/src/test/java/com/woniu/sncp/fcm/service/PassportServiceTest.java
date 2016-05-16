package com.woniu.sncp.fcm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.FcmApplication;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.passport.service.PassportService;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(FcmApplication.class)
public class PassportServiceTest {
	
	@Autowired
	private PassportService passportService;

	@Test
	public void findPassportByAccount() {
		PassportDto passportDto = null;
		try {
			passportDto = passportService.findPassportByAccountOrAliase("test123");
		} catch (PassportNotFoundException | PassportHasFrozenException | PassportHasLockedException
				| SystemException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(passportDto);
	}
}
