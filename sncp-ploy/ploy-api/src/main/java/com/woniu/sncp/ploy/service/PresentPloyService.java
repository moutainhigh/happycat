package com.woniu.sncp.ploy.service;

import java.util.Date;
import java.util.List;

import com.woniu.sncp.ploy.dto.PresentsPloyDTO;



/**
 * 活动服务接口
 * @author chenyx
 *
 */
public interface PresentPloyService {

	/**
	 * 查询有效的活动列表
	 * @param gameId 游戏ID
	 * @param eventTime　活动参与时间
	 * @return
	 * @throws Exception
	 */
	public List<PresentsPloyDTO> findByGameId(String gameId, Date eventTime) throws Exception;
	
	public List<Present>
}
