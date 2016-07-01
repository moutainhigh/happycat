package com.woniu.sncp.profile.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@SuppressWarnings("rawtypes")
@NoRepositoryBean
public interface CardManageRepository extends JpaRepository{

	
	String cardValueSql = "select distinct m.n_id id,m.s_name dispName,t.n_define_min_price minValue,decode(m.s_type, '1', t.s_currency) currency,m.s_type type,decode(m.s_type, '1', t.N_MONEY) cardPoint,decode(m.s_type, '1', t.n_price) cardPrice,decode(m.s_type, '1', cd.s_name) currencyName,t.s_flag customValueFlag,m.n_cardtype_id cardId,c.n_payment_id paymentId,m.n_order dispOrder from sn_card.CARD_DENOMINATION m,sn_card.CARD_DENOMINATION_DETAIL d,sn_card.CARD_TYPE_COMPARISON c,sn_account.ACC_CURRENCY_DEF cd,sn_card.CARD_TYPE t where 1=1  and m.n_id = d.n_main_id and ((m.n_cardtype_id = c.n_cardtype_id and d.n_cardtype_id is null) or (d.n_cardtype_id = c.n_cardtype_id)) and c.n_cardtype_id = t.n_id and t.s_currency = cd.s_currency and m.n_game_id = t.n_game_id and m.s_state = '1' and c.s_type in ('1', '2')  and m.n_game_id = :gameId and c.n_payment_id = :platformId";
	
	
	@Query(value = cardValueSql, nativeQuery = true)
	List<Object[]> findValueByGameIdAndPlatformId(Long gameId, Long platformId);
}
