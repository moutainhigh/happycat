package com.woniu.sncp.passport.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.passport.PassportApplication;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PassportApplication.class)
public class PassportServiceImplTest {
	
	@Autowired
	private PassportService passportService;

	@Test
	public void testFindPassportByAccountOrAliase() {
		PassportDto passportDto = null;
		try {
			passportDto = passportService.findPassportByAccountOrAliase("test123");
		} catch (PassportNotFoundException | PassportHasFrozenException | PassportHasLockedException e) {
			e.printStackTrace();
		}
		assertNotNull(passportDto);
	}

}
