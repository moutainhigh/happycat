package com.woniu.sncp.passport.service;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

/**
 * 帐号相关服务接口
 * 
 * @author chenyx
 * @date 2016年5月4日
 */
public interface PassportService {

	/**
	 * 根据帐号或者别名查询帐号信息
	 * 
	 * @param passportOrAliase
	 * @return 帐号信息{@link com.woniu.sncp.passport.dto.PassportDto}
	 * @throws PassportNotFoundException
	 *             帐号未找到
	 * @throws PassportHasFrozenException
	 *             帐号被冻结
	 * @throws PassportHasLockedException
	 *             帐号被锁定
	 * @throws SystemException
	 *             系统异常
	 */
	public PassportDto findPassportByAccountOrAliase(String passportOrAliase)
			throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, SystemException;

	/**
	 * 根据帐号ID查询帐号信息
	 * @param aid 帐号ID
	 * @return 帐号信息{@link com.woniu.sncp.passport.dto.PassportDto}
	 * @throws PassportNotFoundException 帐号未找到
	 * @throws PassportHasFrozenException 帐号被冻结
	 * @throws PassportHasLockedException 帐号被锁定
	 * @throws SystemException  系统异常
	 */
	public PassportDto findPassportByAid(Long aid)
			throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, SystemException;

}
