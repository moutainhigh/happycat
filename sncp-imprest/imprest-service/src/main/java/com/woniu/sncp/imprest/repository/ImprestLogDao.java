package com.woniu.sncp.imprest.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.woniu.sncp.imprest.dto.ImprestLogDTO;

@Repository
public class ImprestLogDao {

	@Autowired
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
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
	public List<ImprestLogDTO> queryImprestLogs(Long aid, Long gameId, Long areaId, List<Long> platformIds,
			Date startDate, Date endDate, List<Long> speCards) throws DataAccessException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("aid", aid);
		params.put("gameId", gameId);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		StringBuffer querySql = new StringBuffer(
							"SELECT l.N_ID AS id,l.N_AID AS aid,l.N_CARDTYPE_ID AS cardTypeId "
							+ " ,l.N_AMOUNT AS amount,l.S_CARD_NO AS cardNo,l.S_IMPREST_MODE AS imprestMode"
							+ " ,l.N_PAY_PLATFORM_ID AS platformId,l.N_GAME_ID AS gameId,l.N_GAREA_ID AS gameAreaId"
							+ " ,l.S_CURRENCY AS currency,l.N_MONEY AS point,l.N_BATCH_ID AS batchId"
							+ " ,l.D_IMPREST AS imprestDate,l.N_IP AS ip,l.N_IMPREST_PLOY_ID AS ployId"
							+ " ,l.N_GIFT_GAREA_ID AS presentGameAreaId"
				 			+ " FROM SN_IMPREST.IMP_LOG l "
							+ " WHERE l.N_AID = :aid AND l.N_GAME_ID = :gameId  "
							+ " AND l.D_IMPREST >= :startDate AND l.D_IMPREST <= :endDate"
							);
		
		if (areaId != null) {
			querySql.append(" AND l.N_GAREA_ID = :areaId");
			params.put("areaId", areaId);
		}
		
		if (CollectionUtils.isNotEmpty(speCards)) {
			querySql.append(" AND l.N_CARDTYPE_ID IN (:speCards)");
			params.put("speCards", speCards);
		}
		
		if (CollectionUtils.isNotEmpty(platformIds)) {
			querySql.append(" AND l.N_PAY_PLATFORM_ID in (:platformId)");
			params.put("platformId", platformIds);
		}
		
		querySql.append(" ORDER BY l.D_IMPREST");
		
		List<ImprestLogDTO> impLogList = namedParameterJdbcTemplate.query(querySql.toString(), params, new BeanPropertyRowMapper<ImprestLogDTO>(ImprestLogDTO.class));
		
		return impLogList;
	}
}
