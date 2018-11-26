package com.woniu.sncp.pay.core.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.repository.pay.IssuerComparison;
import com.woniu.sncp.pay.repository.pay.IssuerComparisonRepository;

/**
 * 支付平台服务类
 * 
 * @author luzz
 *
 */
@Service("issuerComparisonService")
public class IssuerComparisonService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
 
	@Resource
	private IssuerComparisonRepository issuerComparisonDao;

	public IssuerComparison findIssuerComparison(Long issuerId, String issuerMark) {
	 

		List<IssuerComparison> list = issuerComparisonDao.query(issuerId, issuerMark);
		if (CollectionUtils.isEmpty(list))
			return null;
		return list.get(0);

	}

}
