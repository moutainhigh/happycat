package com.woniu.sncp.profile.service;


import com.woniu.sncp.profile.dto.AllActivityDTO;

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
}
