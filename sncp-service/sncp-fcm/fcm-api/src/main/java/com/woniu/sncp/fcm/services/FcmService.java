package com.woniu.sncp.fcm.services;

import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;

public interface FcmService {
	
	/**
	 * 根据游戏id和帐号id 判断是否是防沉迷帐号
	 * 
	 * @param accountId
	 * @param gameId
	 * @return
	 */
	boolean isFcm(Long accountId,Long gameId);
	
	/**
	 * 根据游戏id和帐号id 更新在线时长和离线时长，并返回在线时长
	 * 
	 * @param accountId
	 * @param gameId
	 * @return 返回在线时长 单位秒
	 */
	Long fcmOnlineTime(Long accountId,Long gameId);
	
	/**
	 * 根据游戏id和帐号id 查询防沉迷信息
	 * 
	 * @param accountId
	 * @param gameId
	 * @return
	 */
	PassportFcmTotalTimeTo queryUserFcmTotalTime(Long accountId,Long gameId);
}
