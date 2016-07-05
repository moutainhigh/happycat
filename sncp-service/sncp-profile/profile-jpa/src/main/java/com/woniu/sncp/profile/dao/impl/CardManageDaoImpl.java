package com.woniu.sncp.profile.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.woniu.sncp.profile.dao.BaseDao;
import com.woniu.sncp.profile.dao.CardManageDao;
import com.woniu.sncp.profile.po.CardValuePo;
import com.woniu.sncp.profile.po.CardDetailPo;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Transactional  
@Repository
public class CardManageDaoImpl extends BaseDao implements CardManageDao {

	/**
	 * 查询大类
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CardValuePo> findValueByGameIdAndPlatformId(Long gameId, Long platformId) {
		StringBuffer sbf = new StringBuffer();
		sbf.setLength(0);
		Map<String, Object> params = new HashMap<String, Object>();
		//oracle
		//sbf.append("select distinct m.n_id id,m.s_name dispName,t.n_define_min_price minValue,decode(m.s_type, '1', t.s_currency) currency,m.s_type type,decode(m.s_type, '1', t.N_MONEY) cardPoint,decode(m.s_type, '1', t.n_price) cardPrice,decode(m.s_type, '1', cd.s_name) currencyName,t.s_flag customValueFlag,m.n_cardtype_id cardId,c.n_payment_id paymentId,m.n_order dispOrder");
		//mysql
		sbf.append("select distinct m.n_id id,m.s_name dispName,t.n_define_min_price minValue,CASE m.s_type WHEN '1' THEN t.s_currency END currency,m.s_type type,CASE m.s_type WHEN '1' THEN t.N_MONEY END cardPoint,CASE m.s_type WHEN '1' THEN t.n_price END cardPrice,CASE m.s_type WHEN '1' THEN cd.s_name END currencyName,t.s_flag customValueFlag,m.n_cardtype_id cardId,c.n_payment_id paymentId,m.n_order dispOrder");
		sbf.append(" from CARD_DENOMINATION m,CARD_DENOMINATION_DETAIL d,CARD_TYPE_COMPARISON c,ACC_CURRENCY_DEF cd,CARD_TYPE t");
		sbf.append(" where 1=1 ");
		sbf.append(" and m.n_id = d.n_main_id and ((m.n_cardtype_id = c.n_cardtype_id and d.n_cardtype_id is null) or (d.n_cardtype_id = c.n_cardtype_id)) and c.n_cardtype_id = t.n_id and t.s_currency = cd.s_currency and m.n_game_id = t.n_game_id");
		sbf.append(" and m.s_state = '1' and c.s_type in ('1', '2') ");
		if(gameId>0){
			sbf.append(" and m.n_game_id = :gameId");
			params.put("gameId", gameId);
		}
		if(platformId>0){
			sbf.append(" and c.n_payment_id = :platformId");
			params.put("platformId", platformId);
		}
		sbf.append(" order by c.n_payment_id, m.n_order");
		return (List<CardValuePo>) super.queryListEntity(sbf.toString(), params, CardValuePo.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CardDetailPo> findDetailByGameIdAndPlatformId(Long gameId, Long platformId) {
		StringBuffer sbf = new StringBuffer();
		sbf.setLength(0);
		Map<String, Object> params = new HashMap<String, Object>();
		sbf.append("select d.n_id id,d.n_main_id mainId,d.s_name valueName,d.s_note valueDesc,d.n_price price,d.s_state state,c.n_payment_id paymentId,d.n_order dispOrder,t.n_id cardId,t.n_price cardPrice,t.N_MONEY cardPoint,t.s_state tState, CASE m.s_type WHEN '2' THEN t.s_currency END currency, CASE m.s_type WHEN '2' THEN cd.s_name END currencyName");
		sbf.append(" from CARD_DENOMINATION m,CARD_DENOMINATION_DETAIL d,CARD_TYPE_COMPARISON c,CARD_TYPE t,ACC_CURRENCY_DEF cd");
		sbf.append(" where 1=1 ");
		sbf.append(" and m.n_id = d.n_main_id and ((m.n_cardtype_id = c.n_cardtype_id and d.n_cardtype_id is null) or d.n_cardtype_id = c.n_cardtype_id) and c.n_cardtype_id = t.n_id and t.s_currency = cd.s_currency");
		sbf.append(" and t.s_state = '0' and d.s_state in ('1', '5', '3')");
		if(gameId>0){
			sbf.append(" and m.n_game_id = :gameId");
			params.put("gameId", gameId);
		}
		if(platformId>0){
			sbf.append(" and c.n_payment_id = :platformId");
			params.put("platformId", platformId);
		}
		sbf.append(" order by c.n_payment_id, m.n_id, d.n_order");
		return (List<CardDetailPo>) super.queryListEntity(sbf.toString(), params,CardDetailPo.class);
	}
	
}
