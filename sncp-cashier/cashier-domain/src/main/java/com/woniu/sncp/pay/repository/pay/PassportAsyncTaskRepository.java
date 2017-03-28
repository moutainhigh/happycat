package com.woniu.sncp.pay.repository.pay;


import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * <p>descrption: 账号操作</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface PassportAsyncTaskRepository extends JpaRepository<PassportAsyncTask, Long> {

	
	PassportAsyncTask save(PassportAsyncTask messageQueue);
	
	PassportAsyncTask findByOperationIdAndTaskType(String[] properties, Object[] values);
	
	@Modifying(clearAutomatically = true)
	@Query("update PassportAsyncTask task set task.modifyDate =:now where task.state =:state")
	void updatePpAsyncTask(Date now, String state);
}
