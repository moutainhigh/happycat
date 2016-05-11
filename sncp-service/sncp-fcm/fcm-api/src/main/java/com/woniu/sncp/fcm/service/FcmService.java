package com.woniu.sncp.fcm.service;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

/**
 * 防沉迷接口
 * @author chenyx
 * @date 2016年5月6日
 */
public interface FcmService {

	/**
	 * 根据游戏id和帐号id 判断是否是防沉迷帐号
	 * 
	 * 数据库异常 为保证服务可用，返回不防沉迷
	 * 
	 * @param accountId 蜗牛通行证Id
	 * @param aoId 运营商Id
	 * @param gameId 游戏Id
	 * @param validThreeCondition 
	 * 				true 18周岁 已审核，审核中和3天内注册 不防沉迷
	 * 				false 仅 18周岁 已审核 不防沉迷
	 * 
	 * @return true:需要防沉迷，false:不需要防沉迷
	 */
	boolean isFcm(Long accountId,Long aoId,Long gameId,boolean validateThreeCondition) throws MissingParamsException, PassportNotFoundException;
	
	/**
	 * 根据帐号id查询 防沉迷唯一标识
	 * 
	 * @param accountId 蜗牛通行证Id
	 * @return
	 */
	String queryIdentity(Long accountId) throws MissingParamsException,PassportNotFoundException;
	
	/**
	 * 根据游戏id和帐号id 更新在线时长和离线时长，并返回在线时长
	 * 
	 * 数据库异常 为保证服务可用，返回在线时长为0
	 * 
	 * @param identity 防沉迷唯一标识
	 * @param gameId
	 * @return 返回在线时长 单位秒
	 */
	Long fcmOnlineTime(String identity,Long gameId) throws MissingParamsException,SystemException;
	
	/**
	 * 根据游戏id和帐号id 查询防沉迷信息
	 * 
	 * @param identity 防沉迷唯一标识
	 * @param gameId
	 * @return PassportFcmTotalTimeTo {@link com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo}
	 * @throws MissingParamsException 缺少参数异常
	 */
	PassportFcmTotalTimeTo queryUserFcmTotalTime(String identity,Long gameId) throws MissingParamsException, SystemException;
}
