package com.woniu.sncp.imprest.service;

import java.util.Date;
import java.util.List;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;
import com.woniu.sncp.imprest.dto.ImprestOrderDTO;

/**
 * 充值服务
 * @author chenyx
 *
 */
public interface ImprestService {

	/**
	 * 根据充值日志ID查询充值日志
	 * @param implogId 充值日志ID
	 * @return　充值日志
	 */
	public ImprestLogDTO findImprestLogById(Long implogId) throws SystemException;
	
	/**
	 * 根据订单号、分区、帐号ID查询充值订单
	 * @param orderNo 订单号
	 * @param gameAreaId 分区ID
	 * @param aid 帐号ID
	 * @return　充值订单
	 */
	public ImprestOrderDTO findImprestOrderByOrderNoAndGameAreaIdAndAid(String orderNo, Long gameAreaId, Long aid) throws SystemException;
	
	/**
	 * 根据卡主键查询充值卡信息
	 * @param cardId
	 * @return
	 */
	public ImprestCardTypeDTO findImprestCardById(Long cardId) throws SystemException;
	
	/**
	 * 查询充值记录列表，根据充值时间排序
	 * 
	 * @param aid 通行证数字帐号
	 * @param gameId 游戏id
	 * @param areaId 分区id 可以为空，为空时不根据该条件查询
	 * @param platformId 支付平台id 可以为空，为空时不根据该条件查询
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param speCards 特殊卡id列表 可以为空，为空时不根据该条件查询
	 * @return
	 * @throws DataAccessException
	 */
	public List<ImprestLogDTO> queryImprestLogs(Long aid, Long gameId, 
			Long areaId, List<Long> platformIds, Date startDate, Date endDate,
			List<Long> speCards)  throws SystemException;
	
	/**
	 * 查询累计赠点
	 * @param aid 帐号ID
	 * @param start 开始时间
	 * @param end 结束时间
	 * @param currency 货币
	 * @param sourceType 赠送来源 可以为空
	 * @return
	 * @throws SystemException
	 */
	public Long findSumLargessPoints(Long aid,  Date start, Date end,  String currency, String sourceType) throws SystemException;
	
	/**
	 * 查询充值总额
	 * @param gameId 游戏ID
	 * @param aid 帐号ID
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return
	 * @throws SystemException
	 */
	public Long findSumImprestAmount(Long gameId,  Long aid, Date start, Date end) throws SystemException;

}
