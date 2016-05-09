package com.woniu.sncp.fcm.service;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;

/**
 * 防沉迷接口
 * @author chenyx
 * @date 2016年5月6日
 */
public interface FcmService {

	/**
	 * 根据游戏id和帐号id 判断是否是防沉迷帐号
	 * 
	 * @param accountId
	 * @param gameId
	 * @return true:需要防沉迷，false:不需要防沉迷
	 */
	boolean isFcm(Long accountId,Long gameId) throws MissingParamsException, SystemException;
	
	/**
	 * 根据游戏id和帐号id 更新在线时长和离线时长，并返回在线时长
	 * 
	 * @param accountId 
	 * @param gameId
	 * @return 返回在线时长 单位秒
	 */
	Long fcmOnlineTime(Long accountId,Long gameId) throws MissingParamsException, SystemException;
	
	/**
	 * 根据游戏id和帐号id 查询防沉迷信息
	 * 
	 * @param accountId
	 * @param gameId
	 * @return PassportFcmTotalTimeTo {@link com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo}
	 * @throws MissingParamsException 缺少参数异常
	 */
	PassportFcmTotalTimeTo queryUserFcmTotalTime(Long accountId,Long gameId) throws MissingParamsException, SystemException;
}
