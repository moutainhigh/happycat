package com.woniu.sncp.pay.core.service.schedule;

import java.util.Map;

import com.woniu.sncp.pay.repository.pay.MessageQueue;
import com.woniu.sncp.pay.repository.pay.PassportAsyncTask;


/**
 * <p>descrption: 调度接口</p>
 * 
 * @author fuzl
 * @date   2015年12月21日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface Schedule {

	PassportAsyncTask createSchedule(Map<String,Object> params);
	
	void updateSchedule(PassportAsyncTask task,String state);
	
	String executeSchedule(PassportAsyncTask task) throws Exception;

	PassportAsyncTask querySchedule(Long newScheduleId);
	
	PassportAsyncTask querySchedule(Long operationId,String taskType);

	MessageQueue queryMessageQueue(Long id, Long taskType);

	MessageQueue createMessageQueue(Map<String, Object> inParams);
}
