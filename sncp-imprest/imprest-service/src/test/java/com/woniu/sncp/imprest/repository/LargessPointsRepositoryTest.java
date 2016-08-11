package com.woniu.sncp.imprest.repository;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.expr.BooleanExpression;
import com.woniu.sncp.imprest.ImprestApplication;
import com.woniu.sncp.imprest.entity.LargessPoints;
import com.woniu.sncp.imprest.entity.QLargessPoints;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImprestApplication.class)
public class LargessPointsRepositoryTest {
	
	@Autowired
	private LargessPointsRepository largessPointsRepository;

	@Test
	public void testFindOnePredicate() {
		LargessPoints largessPoints = new LargessPoints();
		largessPoints.setCurrency("D");
		largessPoints.setAid(1502099031L);
		QLargessPoints qLargessPoints = QLargessPoints.largessPoints;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qLargessPoints.currency.eq("D")).and(qLargessPoints.aid.eq(1502099031L));
		BooleanExpression right = null;
		if(StringUtils.hasText(largessPoints.getCurrency())) {
			right = qLargessPoints.currency.eq("D");
		}
		if(largessPoints.getAid() != null) {
			right = right.and(qLargessPoints.aid.eq(largessPoints.getAid()));
		}
		Iterable<LargessPoints> result = largessPointsRepository.findAll(right);
		for(LargessPoints points : result) {
			System.out.println(points.getId());
		}
		assertTrue(true);
	}
	
	@Test
	public void testSumAmountByAidAndCreateDateAndCurrency() {
		Calendar s = Calendar.getInstance();
		s.set(2012, 0, 4, 9, 30, 0);
		Calendar e = Calendar.getInstance();
		e.set(2012, 0, 4, 9, 40, 0);
		Long sumAmount = largessPointsRepository.sumAmountByAidAndCreateDateAndCurrency(1502099031L, s.getTime(), e.getTime(), "D");
		assertTrue(sumAmount > 0);
	}
	
	@Test
	public void testSumAmountByAidAndCreateDateAndCurrencyAndSourceType() {
		Calendar s = Calendar.getInstance();
		s.set(2012, 0, 4, 9, 30, 0);
		Calendar e = Calendar.getInstance();
		e.set(2012, 0, 4, 9, 40, 0);
		Long sumAmount = largessPointsRepository.sumAmountByAidAndCreateDateAndCurrencyAndSourceType(1502099031L, s.getTime(), e.getTime(), "D", "e");
		assertTrue(sumAmount > 0);
	}

}
