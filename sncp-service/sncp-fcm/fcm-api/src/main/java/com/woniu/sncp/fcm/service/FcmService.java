package com.woniu.sncp.fcm.service;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.FcmIsNeedReqDto;
import com.woniu.sncp.fcm.dto.FcmTimeOnlineReqDto;
import com.woniu.sncp.fcm.dto.FcmTimeOnlineRespDto;

/**
 * 防沉迷接口
 * @author chenyx
 * @date 2016年5月6日
 */
public interface FcmService {

	/**
	 * 判断玩家是否需要防沉迷
	 * @param fcmIsNeedRequestDto 请求参数 {@link com.woniu.sncp.fcm.dto.FcmIsNeedReqDto}
	 * @return true:需要防沉迷，false:不需要防沉迷
	 * @throws MissingParamsException 缺少参数异常
	 * @throws SystemException 系统异常
	 */
	public Boolean isNeedingFcm(FcmIsNeedReqDto fcmIsNeedRequestDto) throws MissingParamsException, SystemException;
	
	/**
	 * 在线时长查询和累计
	 * @param fcmTimeOnlineReqDto 请求参数{@link com.woniu.sncp.fcm.dto.FcmTimeOnlineReqDto}
	 * @return FcmTimeOnlineRespDto {@link com.woniu.sncp.fcm.dto.FcmTimeOnlineRespDto}
	 * @throws MissingParamsException 缺少参数异常
	 * @throws SystemException 系统异常
	 */
	public FcmTimeOnlineRespDto timeOnlineAccumulative(FcmTimeOnlineReqDto fcmTimeOnlineReqDto) throws MissingParamsException, SystemException;
	
}
