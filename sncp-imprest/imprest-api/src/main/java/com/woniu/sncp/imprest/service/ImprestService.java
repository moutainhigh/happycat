package com.woniu.sncp.imprest.service;

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
	public ImprestOrderDTO findImprestOrderByOrderNoAndGameAreaIdAndAid(String orderNo, Long gameAreaId, Long aid) throws SystemException;;
	
	/**
	 * 根据卡主键查询充值卡信息
	 * @param cardId
	 * @return
	 */
	public ImprestCardTypeDTO findImprestCardById(Long cardId) throws SystemException;;
	
	
}
