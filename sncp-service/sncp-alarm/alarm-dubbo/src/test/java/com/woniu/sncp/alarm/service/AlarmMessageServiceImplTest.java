package com.woniu.sncp.alarm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.alarm.AlarmApplication;
import com.woniu.sncp.alarm.dto.AlarmMessageTo;
import com.woniu.sncp.alarm.service.AlarmMessageService;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(AlarmApplication.class)
public class AlarmMessageServiceImplTest {
	
	@Autowired AlarmMessageService alarmMessageService;
	
	@Test public void testSendMessage(){
		alarmMessageService.sendMessage(new AlarmMessageTo("10000100023", "内网测试！"));
	}
	
}
