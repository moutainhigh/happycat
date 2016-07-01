package com.woniu.sncp.fcm.integration;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.woniu.sncp.fcm.ScheduleApplication;
import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ScheduleApplication.class)
public class FmcScheduleActivatorTest {

	@Autowired
	private MongoTemplate mongoTemplate;

	//@Test
	public void testInsertMongo() {
		Calendar calendar = Calendar.getInstance();
		PassportFcmTotalTimePo objectToSave = new PassportFcmTotalTimePo();
		objectToSave.setGameId(10L);
		objectToSave.setIdentity("320503198702062017");
		objectToSave.setLastChange(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(calendar.getTime()));
		objectToSave.setLeaveTime(10L);
		objectToSave.setTime(2000L);
		mongoTemplate.insert(objectToSave);
	}

	@Test
	public void testSeachMongo() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PassportFcmTotalTimePo> lists = mongoTemplate.find(query(where("lastChange").gt(calendar.getTime())),
				PassportFcmTotalTimePo.class);
		for (PassportFcmTotalTimePo po : lists) {
			System.out.println(sdf.format(po.getLastChange()));
		}
	}

}
