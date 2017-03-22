package com.woniu.sncp.pay.repository.pay;

import org.springframework.data.repository.CrudRepository;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */

public interface PaymentMerchantRepository extends CrudRepository<PaymentMerchant, Long> {

	PaymentMerchant findById(Long Id);
}
