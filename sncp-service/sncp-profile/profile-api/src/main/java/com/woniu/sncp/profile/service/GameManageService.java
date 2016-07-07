package com.woniu.sncp.profile.service;

import java.util.List;

import com.woniu.sncp.profile.dto.GameAreaDTO;
import com.woniu.sncp.profile.dto.GameGroupDTO;

/**
 * 
 * <p>descrption: 游戏网络类型和分区接口</p>
 * 
 * @author fuzl
 * @date   2016年7月7日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface GameManageService {

	
	/**
	 * @param serverId 服务器ID
	 * @return
	 */
	GameAreaDTO findByServerId(Long serverId);
	/**
	 * 根据游戏id和状态、类型获取游戏网络类型和分区信息
	 * @param gameId
	 * @param state
	 * @param type
	 * @return
	 */
	List<GameGroupDTO> findByGameIdAndStateAndType(Long gameId,String state,String type);
}
