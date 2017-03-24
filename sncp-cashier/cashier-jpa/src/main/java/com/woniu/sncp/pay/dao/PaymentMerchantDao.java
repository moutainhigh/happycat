package com.woniu.sncp.pay.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.woniu.sncp.pay.dao.PaymentMerchantDao;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentMerchantDetail;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional  
@Repository
public class PaymentMerchantDao extends PayBaseDao{
	
	@SuppressWarnings("unchecked")
	public List<PaymentMerchantDetail> queryPaymentMerchantDtlById(Long id) {
		StringBuffer sbf = new StringBuffer();
		sbf.setLength(0);
		Map<String, Object> params = new HashMap<String, Object>();
		//mysql
		sbf.append("SELECT D.*,(SELECT P.S_NAME FROM SN_PAY.PAY_PLATFORM P WHERE  P.N_ID  = IFNULL(D.S_BANK_PLATFORM_ID,D.S_CONTENT)) S_NAME " +
				" FROM SN_PAY.PAY_MERCHANT_DTL D WHERE D.S_STATUS = '1' ");
		if(id>0){
			sbf.append(" AND D.N_MERCHANT_ID = " + id);
//			params.put("id", id);
		}
		sbf.append(" ORDER BY D.N_SORT,D.S_TYPE");
		return (List<PaymentMerchantDetail>) super.queryListEntity(sbf.toString(), params, PaymentMerchantDetail.class);
	}

	@SuppressWarnings("unchecked")
	public PaymentMerchant queryPayemntMerchnt(long merchantId){
		if(ObjectUtils.isEmpty(merchantId)){
			return null;
		}
		String querySql = "SELECT * FROM SN_PAY.PAY_MERCHANT M WHERE M.S_STATUS = '1' AND M.N_ID =:merchantId";
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("merchantId", merchantId);
		List<PaymentMerchant> result = (List<PaymentMerchant>) super.queryListEntity(querySql.toString(), paramMap, PaymentMerchant.class);
		return result.isEmpty()?null:result.get(0);
	}
}
