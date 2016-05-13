package com.woniu.sncp.alarm.service;

import com.woniu.sncp.alarm.dto.AlarmMessageTo;

/**
 * 告警消息服务类
 * 
 * @author luzz
 *
 */
public interface AlarmMessageService {
	
	/**
	 * 告警消息异步发送
	 * 
	 * @param monitorMessage {@link com.woniu.sncp.monitor.dto.AlarmMessageTo}
	 */
	void sendMessage(AlarmMessageTo alarmMessage);
}
