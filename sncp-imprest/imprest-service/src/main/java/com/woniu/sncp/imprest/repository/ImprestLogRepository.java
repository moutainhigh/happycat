package com.woniu.sncp.imprest.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.imprest.entity.ImprestLog;

/**
 * 充值日志相关数据库Respository
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
	public Long findSumAmountAndPriceByGameIdAndAidAndImprestDate(@Param("gameId") Long gameId, @Param("aid") Long aid,
			@Param("start") Date start, @Param("end") Date end);
}
