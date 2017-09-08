package com.woniu.sncp.pay.repository.pay;


import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
 
	
}
