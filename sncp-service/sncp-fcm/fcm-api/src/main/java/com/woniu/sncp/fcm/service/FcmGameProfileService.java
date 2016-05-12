package com.woniu.sncp.fcm.service;

import java.util.List;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;

/**
 * 防沉迷配置接口
 * @author luzz
 * @since JDK1.8
 * @version 1.0.0
 */
public interface FcmGameProfileService {

	/**
	 * 创建或更新防沉迷游戏配置
	 * 
	 * @param fcmGameProfileTo 防沉迷配置对象
	 * @throws MissingParamsException 参数缺少异常
	 * @throws SystemException 系统异常
	 */
	void save(FcmGameProfileTo fcmGameProfileTo) throws MissingParamsException, SystemException;
	
	/**
	 * 删除防沉迷游戏配置
	 * 
	 * @param aoId 主键
	 * @param gameId 游戏ID
	 * @return 1成功，其他失败
	 * @throws MissingParamsException 参数缺少异常
	 * @throws SystemException 系统异常
	 */
	Long delete(Long aoId,Long gameId) throws MissingParamsException, SystemException;
	
	/**
	 * 查询防沉迷游戏配置
	 * 
	 * @param aoId 主键
	 * @return 防沉迷游戏配置列表
	 * @throws MissingParamsException 参数缺少异常
	 * @throws SystemException 系统异常
	 */
	List<FcmGameProfileTo> query(Long aoId) throws MissingParamsException, SystemException;
	
	/**
	 * 查询防沉迷游戏配置
	 * 
	 * @param aoId 主键
	 * @param gameId 游戏ID
	 * @return 防沉迷游戏配置
	 * @throws MissingParamsException 参数缺少异常
	 * @throws SystemException 系统异常
	 */
	FcmGameProfileTo query(Long aoId,Long gameId) throws MissingParamsException, SystemException;
}
