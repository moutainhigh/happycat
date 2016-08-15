package com.woniu.sncp.imprest.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.imprest.entity.ImprestLog;

/**
 * 充值日志相关数据库Respository
 * 
 * @author chenyx
 *
 */
public interface ImprestLogRepository extends JpaRepository<ImprestLog, Long> {

	/**
	 * 查询一段时间内的充值总额
	 * 
	 * @param gameId
	 * @param aid
	 * @param start
	 * @param end
	 * @return
	 */
	@Query("select sum(l.amount * c.price) from ImprestLog l, ImprestCardType c where c.id = l.cardTypeId and l.gameId = :gameId "
			+ "and l.aid = :aid and l.imprestDate between :start and :end")
	public BigDecimal findSumAmountAndPriceByGameIdAndAidAndImprestDate(@Param("gameId") Long gameId, @Param("aid") Long aid,
			@Param("start") Date start, @Param("end") Date end);

	/**
	 * 查询帐号的充值总额
	 * @param aid 帐号ID
	 * @param gameId 游戏ID
	 * @param gAreaIds 分区ID，多个使用“，”分割
	 * @param notImprestMode 非充值模式
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return
	 */
	@Query(value = "select sum(case when l.s_imprest_mode='B' "
			+ "then (select o.n_money from sn_imprest.imp_order o where o.s_order_no = l.s_card_no and o.s_state = '1') "
			+ "else (l.n_amount * c.n_price) end) "
			+ "from sn_imprest.imp_log l, sn_card.card_type c where c.n_id = l.n_cardtype_id and l.n_aid = :aid and "
			+ "l.n_game_id = :gameId and l.n_garea_id in :gAreaIds and"
			+ " l.s_imprest_mode <> :notImprestMode and l.d_imprest between :start and :end group by l.n_aid, l.n_game_id", nativeQuery = true)
	public BigDecimal findSumAmountByAidAndGameIdAndGameAreaIdAndImprestDate(@Param("aid") Long aid, @Param("gameId") Long gameId,
			@Param("gAreaIds") List<Long> gAreaIds, @Param("notImprestMode") String notImprestMode,
			@Param("start") Date start, @Param("end") Date end);
	
}
