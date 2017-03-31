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
	@Query("update PaymentOrder paymentOrder set paymentOrder.payState =:payState where paymentOrder.orderId =:orderId")
	void updateS(@Param("orderId") Long orderId, @Param("payState") String payState);
	
	@Modifying(clearAutomatically = true)
	@Query("update PaymentOrder paymentOrder set paymentOrder.payState =:payState,paymentOrder.state =:state where paymentOrder.orderId =:orderId")
	void updateSS(@Param("orderId") Long orderId, @Param("payState") String payState, @Param("state") String state);
	
	@Modifying(clearAutomatically = true)
	@Query("update PaymentOrder paymentOrder set paymentOrder.state =:state where paymentOrder.orderId =:orderId")
	void updateIS(@Param("orderId") Long id, @Param("state") String state);
	
	
	//第三方,合作方订单号
	PaymentOrder findByPaypartnerOtherOrderNo(String partnerOrderNo);

	//对方渠道订单号
	PaymentOrder findByOtherOrderNo(String otherOrderN);
	
	//我方订单号
	PaymentOrder findByOrderNo(String orderNo);
	
	//业务商户号,合作方订单号
	PaymentOrder findByMerchantIdAndPaypartnerOtherOrderNo (Long merchantId,String partnerOrderNo);
}
