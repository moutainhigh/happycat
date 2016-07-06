package com.woniu.sncp.profile.service;

import java.util.List;

import com.woniu.sncp.profile.dto.GameAreaDTO;
import com.woniu.sncp.profile.dto.GameGroupDTO;

/**
 * @author fuzl
 *
 */
public interface GameManageService {

	
	/**
	 * @param serverId 服务器ID
	 * @return
	 */
	GameAreaDTO findByServerId(Long serverId);
	
	List<GameGroupDTO> findByGameIdAndStateAndType(Long gameId,String state,String type);
}
