package com.woniu.sncp.imprest.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.imprest.entity.LargessPoints;

/**
 * 赠点数据
 * @author chenyx
 *
 */
public interface LargessPointsRepository
		extends JpaRepository<LargessPoints, Long>, QueryDslPredicateExecutor<LargessPoints> {

	@Query("select sum(l.point + l.token) from LargessPoints l where l.aid = :aid and l.createDate between :start and :end and l.currency = :currency")
	public Long sumAmountByAidAndCreateDateAndCurrency(@Param("aid") Long aid, @Param("start") Date start,
			@Param("end") Date end, @Param("currency") String currency) throws SystemException;
	
	@Query("select sum(l.point + l.token) from LargessPoints l where l.aid = :aid and l.createDate between :start and :end and l.currency = :currency and l.sourceType = :sourceType")
	public Long sumAmountByAidAndCreateDateAndCurrencyAndSourceType(@Param("aid") Long aid, @Param("start") Date start,
			@Param("end") Date end, @Param("currency") String currency, @Param("sourceType") String sourceType) throws SystemException;
}
