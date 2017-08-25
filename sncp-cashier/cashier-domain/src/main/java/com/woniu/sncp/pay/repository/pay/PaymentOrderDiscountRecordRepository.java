package com.woniu.sncp.pay.repository.pay;


import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.woniu.sncp.pojo.payment.PaymentOrderDiscount;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscountRecord;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface PaymentOrderDiscountRecordRepository extends JpaRepository<PaymentOrderDiscountRecord, Long> {
//"from  PaymentOrderDiscount where merchantId=? and paymentId=? and state=? and validityPeriodStart<=? and validityPeriodEnd>=?",new Object[] {Long.valueOf(merchantId),Long.valueOf(paymentId),"1",time,time}
	@Query("from  PaymentOrderDiscount where merchantId=:merchantId  and paymentId=:paymentId and state='1' and validityPeriodStart<=:now and validityPeriodEnd>=:now")
	List<PaymentOrderDiscount> queryOrderDiscount(long merchantId, long paymentId,Date now) ;
	@Query("from  PaymentOrderDiscount where merchantId=:merchantId  and state='1' and validityPeriodStart<=:now and validityPeriodEnd>=:now")
	List<PaymentOrderDiscount> queryOrderDiscount(long merchantId, Date now) ;
	
	
}
