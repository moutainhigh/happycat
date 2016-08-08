package com.woniu.sncp.ploy.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PresentsPloyDao {

	@Autowired
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public long queryJoinedPloyCount(Long ployId, Long gameId, Long gameAreaId, Long userId, Long impLogId)
			throws DataAccessException {

		String sql = "select count(*) from SN_IMPREST.IMP_LARGESS_INFO info where info.N_AID=:userId and info.N_GAME_ID=:gameId ";
		
		if (gameAreaId != null) {
			sql = sql +  " and info.N_GAREA_ID=:gameAreaId ";
		}
		
		sql = sql +  " and info.N_RELATED_ID in ("
				+ " select blog.N_ID from SN_SALES.BUSINESS_LOG blog where blog.N_AID =:userId and blog.N_RELATED_ID =:ployId ";

		if (impLogId != null) {
			sql = sql + " and blog.S_BUSINESS_DATA in ("
					+ " select i.N_ID from SN_IMPREST.IMP_LOG i where i.N_AID=:userId and i.N_GAME_ID =:gameId "
					+ (gameAreaId != null ? " and i.N_GAREA_ID =:gameAreaId " : "")
					//+ " and i.N_ID <  :impLogId "
					+ " and i.D_IMPREST < (select imp.D_IMPREST from SN_IMPREST.IMP_LOG imp where imp.N_ID = :impLogId) " 
					+ " ) ";
		}
		sql = sql + " ) ";
		Map<String, Object> paramValues = new HashMap<String, Object>();
		paramValues.put("userId", userId);
		paramValues.put("gameId", gameId);
		if (gameAreaId != null) {
			paramValues.put("gameAreaId", gameAreaId);
		}
		paramValues.put("ployId", ployId);
		paramValues.put("impLogId", impLogId);

		return namedParameterJdbcTemplate.queryForObject(sql, paramValues, Long.class);
	}
}
