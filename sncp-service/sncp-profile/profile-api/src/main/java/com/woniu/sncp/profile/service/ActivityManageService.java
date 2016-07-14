package com.woniu.sncp.profile.service;


import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.profile.dto.ActivityDTO;
import com.woniu.sncp.profile.dto.AllActivityDTO;
import com.woniu.sncp.profile.exception.ValidationException;

/**
 * <p>descrption: 活动管理接口</p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface ActivityManageService {

	/**
	 * 根据游戏id和状态查询所有活动
	 * @param gameId
	 * @param state
	 * @return 活动信息列表和活动详情列表对象
	 */
	public AllActivityDTO findAllPloysByState(Long gameId,String state);
	/**
	 * 查询具体官方活动对象
	 * @param isEaiQuery  是否eai查询
	 * @param impLogId    充值日志id
	 * @param gameId      游戏id
	 * @param platformId  平台id
	 * @param serverId    服务器id
	 * @param cardTypeId  卡类型id
	 * @param imprestDestination 账户，中心/分区账户
	 * @param count       卡数量
	 * @param passport    面值数量
	 * @param decodeType  加密类型为空
	 * @param valueAmount 保存页面面值数量 ，如果是自定义面值按1元面值计算
	 * @param issuerId    运营商id
	 * @return
	 */
	public ActivityDTO findOfficalPloys(Boolean isEaiQuery,String impLogId,Long gameId,Long platformId,Long areaId,Long cardTypeId,String imprestDestination,Integer count,String passport,String decodeType,String valueAmount,String issuerId);
	/**
	 * 查询一卡通活动对象
	 * @param isEaiQuery  是否eai查询
	 * @param impLogId    充值日志id
	 * @param gameId      游戏id
	 * @param platformId  平台id
	 * @param serverId    服务器id
	 * @param cardTypeId  卡类型id
	 * @param imprestDestination 账户，中心/分区账户
	 * @param count       卡数量
	 * @param passport    面值数量
	 * @param decodeType  加密类型为空
	 * @param valueAmount 保存页面面值数量 ，如果是自定义面值按1元面值计算
	 * @param issuerId    运营商id
	 * @param cardNo      一卡通卡号
	 * @param cardPwd	     一卡通密码
	 * @return
	 * @throws PassportNotFoundException 
	 * @throws MissingParamsException 
	 * @throws ValidationException
	 * @throws PassportHasLockedException 
	 * @throws PassportHasFrozenException 
	 * @throws Exception 
	 */
	public ActivityDTO findSnailCardPloys(Boolean isEaiQuery,String impLogId,Long gameId,Long platformId,Long areaId,Long cardTypeId,String imprestDestination,Integer count,String passport,String decodeType,String valueAmount,String issuerId,String cardNo,String cardPwd) throws MissingParamsException, ValidationException, PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, Exception;
}
