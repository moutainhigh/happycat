package com.woniu.sncp.pay.repository.pay;


import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface MessageQueueRepository extends JpaRepository<MessageQueue, Long> {

	
	MessageQueue save(MessageQueue messageQueue);
	
	MessageQueue findByRelationIdAndTaskType(String[] properties, Object[] values);
}
