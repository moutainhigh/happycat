package com.woniu.sncp.profile.service;

import java.util.List;

import com.woniu.sncp.profile.dto.CardValueDTO;
import com.woniu.sncp.profile.dto.CardDetailDTO;

/**
 * <p>descrption: 卡类型信息接口</p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface CardTypeManageService {

	/**
	 * 根据游戏id和支付平台id获取卡大类信息
	 * @param gameId
	 * @param paymentId
	 * @return
	 */
	List<CardValueDTO> findValueByGameIdAndPlatformId(Long gameId,Long paymentId);
	/**
	 * 根据游戏id和支付平台id获取卡面值信息
	 * @param gameId
	 * @param paymentId
	 * @return
	 */
	List<CardDetailDTO> findDetailByGameIdAndPlatformId(Long gameId,Long paymentId);
}
