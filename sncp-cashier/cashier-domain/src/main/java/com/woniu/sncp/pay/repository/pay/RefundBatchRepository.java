package com.woniu.sncp.pay.repository.pay;


import javax.transaction.Transactional;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface RefundBatchRepository extends JpaRepository<PayRefundBatch, Long> {

	
	PayRefundBatch save(PayRefundBatch payRefundBatch);
	
	@Modifying(clearAutomatically = true)
	@Query("update PayRefundBatch payRefundBatch set payRefundBatch.refundState =:refundState where payRefundBatch.id =:id")
	void updateS(@Param("id") Long id, @Param("refundState") String refundState);
	
	//第三方,合作方订单号
	PayRefundBatch findByPartnerBatchNo(String partnerBatchNo);
	
	//我方订单号
	PayRefundBatch findByBatchNo(String batchNo);

	// 收银台分配业务商户号，业务订单号
	PayRefundBatch findByPartnerIdAndPartnerBatchNo(Long merchantid, String partnerBatchNo);
}
