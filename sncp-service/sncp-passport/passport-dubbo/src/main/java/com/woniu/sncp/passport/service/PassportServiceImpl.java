package com.woniu.sncp.passport.service;

import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

/**
 * 帐号相关接口实现
 * @author chenyx
 * @date 2016年5月4日
 */
public class PassportServiceImpl implements PassportService {

	@Override
	public PassportDto findPassportByAccountOrAliase(String passportOrAliase)
			throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException {
		
		return null;
	}

}
