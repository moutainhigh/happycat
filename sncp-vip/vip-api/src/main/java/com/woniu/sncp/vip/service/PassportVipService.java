package com.woniu.sncp.vip.service;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.vip.dto.PassportVipDTO;

/**
 * 帐号vip服务
 * @author chenyx
 *
 */
public interface PassportVipService {

	/**
	 * 根据帐号ID和游戏ID查询帐号所在游戏的vip级别
	 * @param aid 帐号ID
	 * @param gameId 游戏ID
	 * @return Vip信息
	 * @throws SystemException
	 */
	public PassportVipDTO findPassportVipByAidAndGameId(Long aid, Long gameId) throws SystemException;
}
