package com.woniu.sncp.pay.repository.pay;


import javax.transaction.Transactional;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

	
	PaymentOrder save(PaymentOrder paymentOrder);
	
	@Modifying(clearAutomatically = true)
	@Query("update PaymentOrder paymentOrder set paymentOrder.paymentState =:paymentState where paymentOrder.id =:id")
	void updateS(@Param("id") Long id, @Param("paymentState") String paymentState);
	
	@Modifying(clearAutomatically = true)
	@Query("update PaymentOrder paymentOrder set paymentOrder.paymentState =:paymentState,paymentOrder.imprestState =:imprestState where paymentOrder.id =:id")
	void updateSS(@Param("id") Long id, @Param("paymentState") String paymentState, @Param("imprestState") String imprestState);
	
	@Modifying(clearAutomatically = true)
	@Query("update PaymentOrder paymentOrder set paymentOrder.imprestState =:imprestState where paymentOrder.id =:id")
	void updateIS(@Param("id") Long id, @Param("imprestState") String imprestState);
	
	
	//第三方,合作方订单号
	PaymentOrder findByPartnerOrderNo(String oppositeOrderNo);

	//对方渠道订单号
	PaymentOrder findByPayPlatformOrderId(String pOrderNo);
	
	//我方订单号
	PaymentOrder findByOrderNo(String orderNo);
	
	//业务商户号,合作方订单号
	PaymentOrder findByMerchantIdAndPartnerOrderNo (Long merchantId,String partnerOrderNo);
}
