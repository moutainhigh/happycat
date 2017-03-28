package com.woniu.sncp.pay.repository.pay;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface RefundBatchDetailRepository extends JpaRepository<PayRefundBatchDetail, Long> {

	
	PayRefundBatchDetail save(PayRefundBatchDetail payRefundBatchDetail);
	
	@Modifying(clearAutomatically = true)
	@Query("update PayRefundBatchDetail payRefundBatchDetail set payRefundBatchDetail.refundState =:refundState where payRefundBatchDetail.id =:id")
	void updateS(@Param("id") Long id, @Param("refundState") String refundState);
	
	//第三方,合作方批次号
	PayRefundBatchDetail findByPartnerBatchNo(String partnerBatchNo);
	
	//我方批次号
	PayRefundBatchDetail findByBatchNo(String batchNo);
	
	//我方批次号,合作方批次号
	PayRefundBatchDetail findByBatchNoAndPartnerBatchNo (String batchNo,String partnerBatchNo);
	
	//业务商户号,合作方批次号
	PayRefundBatchDetail findByPartnerIdAndPartnerOrderNo (Long partnerId,String partnerOrderNo);
	
	List<PayRefundBatchDetail> findAll(Specification<PayRefundBatchDetail> spec);
	
}
