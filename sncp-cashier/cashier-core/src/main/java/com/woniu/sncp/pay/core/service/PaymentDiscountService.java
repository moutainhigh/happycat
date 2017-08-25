package com.woniu.sncp.pay.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.core.OrderDiscountRecordQuery;
import com.woniu.sncp.pay.core.Pageable;
import com.woniu.sncp.pay.repository.pay.PaymentOrderDiscountRecordRepository;
import com.woniu.sncp.pay.repository.pay.PaymentOrderDiscountRepository;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscount;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscountRecord;

@Service("paymentDiscountService")
public class PaymentDiscountService {
	@Resource
	private PaymentOrderDiscountRepository discountDao;
	@Resource
	private PaymentOrderDiscountRecordRepository discountRecordDao;
	@PersistenceContext

	EntityManager entityManager;

	public List<PaymentOrderDiscount> queryOrderDiscount(long merchantId, long paymentId) {
		// 切换中心库
		// DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);//
		// 网厅需要实时读，不接受延时，所以改到中心库
		// Map<String, Object> param = new HashMap<String, Object>();
		// param.put("state", "1");
		// param.put("merchantId", merchantId);
		// param.put("paymentId", paymentId);
		// List<PaymentOrderDiscount> list = discountDao.findAllByProperty(param);
		Date time = new Date();
		return discountDao.queryOrderDiscount(merchantId, paymentId, time);
		// return discountDao.findAll("from PaymentOrderDiscount where merchantId=? and
		// paymentId=? and state=? and validityPeriodStart<=? and
		// validityPeriodEnd>=?",new Object[]
		// {Long.valueOf(merchantId),Long.valueOf(paymentId),"1",time,time});

	}

	public List<PaymentOrderDiscount> queryOrderDiscount(long merchantId) {
		// 切换中心库
		// DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);//
		// 网厅需要实时读，不接受延时，所以改到中心库
		// Map<String, Object> param = new HashMap<String, Object>();
		// param.put("state", "1");
		// param.put("merchantId", merchantId);
		// param.put("paymentId", paymentId);
		// List<PaymentOrderDiscount> list = discountDao.findAllByProperty(param);
		Date time = new Date();
		return discountDao.queryOrderDiscount(merchantId, time);

		// return discountDao.findAll("from PaymentOrderDiscount where merchantId=? and
		// state=? and validityPeriodStart<=? and validityPeriodEnd>=?",new Object[]
		// {Long.valueOf(merchantId),"1",time,time});

	}

	public void updatePaymentOrderDiscount(PaymentOrderDiscount bean) {

		discountDao.save(bean);
	}

	public void updateRecord(PaymentOrderDiscountRecord bean) {
		discountRecordDao.save(bean);

	}

	public Pageable<PaymentOrderDiscountRecord> queryRecord(OrderDiscountRecordQuery bean) {

		// 切换中心库
		StringBuilder builder = new StringBuilder("from PaymentOrderDiscountRecord where 1=1 ");

		Map<String, Object> param = new HashMap<String, Object>();
		if (bean.getId() != null) {
			builder.append("and id=:id ");
			param.put("id", bean.getId());
		}

		if (bean.getCreateEndDate() != null) {
			builder.append("and createEndDate <=:createEndDate ");
			param.put("createEndDate", bean.getCreateEndDate());
		}
		if (bean.getCreateStartDate() != null) {
			builder.append("and createStartDate >=:createStartDate ");
			param.put("createStartDate", bean.getCreateStartDate());
		}
		if (bean.getDiscountId() != null) {
			builder.append("and discountId=:discountId ");
			param.put("discountId", bean.getDiscountId());
		}
		if (bean.getMerchantId() != null) {
			builder.append("and merchantId=:merchantId ");
			param.put("merchantId", bean.getMerchantId());
		}
		if (StringUtils.isNotBlank(bean.getOrderNo())) {
			builder.append("and orderNo=:orderNo");
			param.put("orderNo", bean.getOrderNo());
		}
		if (StringUtils.isNotBlank(bean.getPartnerOrderNo())) {
			builder.append("and partnerOrderNo=:partnerOrderNo ");
			param.put("partnerOrderNo", bean.getPartnerOrderNo());
		}
		if (bean.getPaymentId() != null) {
			builder.append("and paymentId=:paymentId ");
			param.put("paymentId", bean.getPaymentId());
		}

		// if (StringUtils.isNotBlank(bean.getPayPlatformOrderId())) {
		// builder.append("and payPlatformOrderId=? ");
		// param.add(bean.getPayPlatformOrderId());
		// }

		// int totalCount=(Integer)commonDao.find(builder.toString(),param).get(0);
		// Number totalCount=(Number)discountRecordDao.findWithHql("select count(*)
		// "+builder.toString(),param.toArray());
		javax.persistence.Query query = entityManager.createQuery("select count(*) " + builder.toString());
		for (Map.Entry<String, Object> entry : param.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		Number totalCount = (Number) query.getSingleResult();
		builder.append(" order by createDate");
		Pageable<PaymentOrderDiscountRecord> pageable = new com.woniu.sncp.pay.core.Pageable(totalCount.intValue(),
				bean.getPageSize(), bean.getPageNumber());
		query = entityManager.createQuery("select count(*) " + builder.toString());
		for (Map.Entry<String, Object> entry : param.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		query.setFirstResult(pageable.getStartIndex());
		query.setMaxResults(bean.getPageSize());
		List<PaymentOrderDiscountRecord> list = query.getResultList();
		// List<PaymentOrderDiscountRecord> list=(List<PaymentOrderDiscountRecord>)
		// discountRecordDao.page(builder.toString(),param.toArray(),pageable.getStartIndex(),
		// bean.getPageSize());
		pageable.setItems(list);
		return pageable;
		// return discountRecordDao.findAll(builder.toString(), param.toArray());

	}

}
