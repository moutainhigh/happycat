package com.woniu.sncp.fcm.integration;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.WriteResult;
import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;


/**
 * 定时防沉迷业务逻辑
 * 
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
@Component
public class FmcScheduleActivator {
	
	private Logger logger = LoggerFactory.getLogger(FmcScheduleActivator.class);  

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * 清理过期离线数据
	 * 
	 * @param expireHours
	 *            过期小时数
	 */
	public void cleanFcmOnlineTime(int expireHours) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, expireHours);
		WriteResult result = mongoTemplate.remove(query(where("lastChange").gt(calendar.getTime())),
				PassportFcmTotalTimePo.class);
		logger.info("Schedule cleanFcmOnlineTime count " + result.getN());
	}
}
