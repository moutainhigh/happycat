package com.woniu.sncp.profile.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.woniu.sncp.profile.dao.ActivityManageDao;
import com.woniu.sncp.profile.dao.BaseDao;
import com.woniu.sncp.profile.po.PassportPresentsPloyDetailPo;
import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Transactional  
@Repository
public class ActivityManageDaoImpl extends BaseDao implements ActivityManageDao {

	
	@SuppressWarnings("unchecked")
	@Override
	public List<PassportPresentsPloyPo> findAllByStateAndPloyTypes(String state, List<String> ployTypes) {
		StringBuffer sbf = new StringBuffer();
		sbf.setLength(0);
		Map<String, Object> params = new HashMap<String, Object>();
		sbf.append("select p.n_id as id,p.s_ploy_name as name,p.s_limit_game as limitGame,p.s_limit_operator as limitIssuer,p.s_limit_agent as limitPaymentPlatform,p.d_start as startDate,p.d_end as endDate ");
		sbf.append(" from PP_PRESENTS_PLOY p ");
		sbf.append(" where 1=1 ");
		sbf.append(" and p.s_state = '3'");
		//sbf.append(" AND P.D_START < SYSDATE AND (P.D_END > SYSDATE OR P.D_END IS NULL) ");//oracle
		sbf.append(" and p.d_start < now() and (p.d_end > now() or p.d_end is null) ");//mysql
		if(ployTypes.size()>0){
			sbf.append(" and p.s_type in (:ployTypes)");
			params.put("ployTypes", ployTypes);
		}
		
		sbf.append(" order by p.d_atime desc");
		return (List<PassportPresentsPloyPo>) super.queryListEntity(sbf.toString(), params, PassportPresentsPloyPo.class);
	}

	/* (non-Javadoc)
	 * @see com.woniu.sncp.profile.dao.ActivityManageDao#findAllByStateAndPloyIds(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<PassportPresentsPloyDetailPo> findAllByStateAndPloyIds(List<Integer> ployIds) {
		StringBuffer sbf = new StringBuffer();
		sbf.setLength(0);
		Map<String, Object> params = new HashMap<String, Object>();
		sbf.append("select d.n_ploy_id as id,d.n_ploy_id as ployId,d.n_prop_id propsId,case when d.s_note is null then g.s_prop_name when d.s_note = g.s_prop_name then d.s_note else d.s_note end note,d.n_amount as amount  ");
		sbf.append(" FROM PP_PRESENTS_PLOY_DETAIL d,GAME_PROPS g ");
		sbf.append(" where 1=1 ");
		sbf.append(" and g.n_prop_id = d.n_prop_id ");
		if(ployIds.size()>0){
			sbf.append(" and d.n_ploy_id in (:ployIds) ");
			params.put("ployIds", ployIds);
		}
		
		sbf.append(" order by n_id ");
		return (List<PassportPresentsPloyDetailPo>) super.queryListEntity(sbf.toString(), params, PassportPresentsPloyDetailPo.class);
	}

}
