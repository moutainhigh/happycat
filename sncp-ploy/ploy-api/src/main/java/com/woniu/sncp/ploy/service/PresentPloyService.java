package com.woniu.sncp.ploy.service;

import java.util.Date;
import java.util.List;

import com.woniu.sncp.exception.SystemException;
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
	
    /**
     * 根据活动，游戏，游戏分区，和帐号查询活动参加过几次
     * @param ployId
     * @param gameId
     * @param gameAreaId 可以为空
     * @param userId
     * @param impLogId 可以为空
     * @return
     * @throws DataAccessException
     */
    Long queryJoinedPloyCount(Long ployId, Long gameId, Long gameAreaId, Long userId,Long impLogId) throws SystemException;

}
