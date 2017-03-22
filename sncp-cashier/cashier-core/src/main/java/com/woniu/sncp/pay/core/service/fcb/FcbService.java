package com.woniu.sncp.pay.core.service.fcb;

import java.util.Map;

import com.woniu.pay.pojo.GamePropsCurrency;

public interface FcbService {
	
	/**
	 * 根据主键查询游戏币种表
	 * @param id
	 * @return
	 */
	GamePropsCurrency queryById(long id);
	
	/**
	 * 查询翡翠币余额(HTTP POST)
	 * @param params
	 * @return
	 */
	Map<String, Object> queryAmount(Map<String, Object> params);
	
	/**
	 * 增加翡翠币点数
	 * @param aid
	 * @param gameId
	 * @param areaId
	 * @param orderId
	 * @param money
	 * @return
	 */
	Map<String, Object> addFcbAmount(Long aid,String orderId,Float money);
	
	/**
	 * 扣除翡翠币点数
	 * @param aid
	 * @param orderNo
	 * @param money
	 * @return
	 */
	Map<String, Object> deductFcbAmount(Long aid,String orderNo,Float money,String clientIp);
	
	/**
	 * 身份认证
	 * @param token
	 * @return
	 */
	Map<String, Object> validateToken(String token);
	
	/**
	 * 注册翡翠比身份
	 * 失效时间10分钟
	 * @param fcbAccount
	 * @param fcbPhone
	 * @return
	 */
	boolean registFcbIdentity(String fcbAccount,String fcbPhone);
	
	/**
	 * 验证翡翠身份
	 * @param fcbAccount
	 * @return
	 */
	FcbIdentity hasFcbIdentity(String fcbAccount);
	
}
