package com.woniu.sncp.fcm.service;

import java.util.List;

import com.woniu.sncp.fcm.dto.FcmGameProfileTo;

/**
 * @author luzz
 *
 */
public interface FcmGameProfileService {

	/**
	 * 创建或更新防沉迷游戏配置
	 * 
	 * @param fcmGameProfileTo
	 * @return
	 */
	void save(FcmGameProfileTo fcmGameProfileTo);
	
	/**
	 * 删除防沉迷游戏配置
	 * 
	 * @param aoId
	 * @param gameId
	 * @return 1成功，其他失败
	 */
	Long delete(Long aoId,Long gameId);
	
	/**
	 * 查询防沉迷游戏配置
	 * 
	 * @param id 主键
	 * @return
	 */
	List<FcmGameProfileTo> query(Long aoId);
	
	/**
	 * 查询防沉迷游戏配置
	 * 
	 * @param aoId 
	 * @param gameId 
	 * @return
	 */
	FcmGameProfileTo query(Long aoId,Long gameId);
}
