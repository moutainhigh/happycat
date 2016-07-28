package com.woniu.sncp.ploy.integration;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.domain.PloyParticipator;
import com.woniu.sncp.ploy.entity.PresentsPloy;

@ContextConfiguration(locations = {"/META-INF/spring/persents-ploy-integration.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class AggreatorTest {
	
	@Autowired
	MessageChannel ployParticipator;
	
	@Autowired
	private PollableChannel returnChannel;

	
	@Test
	public void testAggreator() {
		final TimedPollableChannel timed = new TimedPollableChannel(returnChannel);
		ployParticipator.send(MessageBuilder.withPayload(createPloyParticipator()).build());
		final Message<?> message = timed.receive(2500);
		System.out.println(message);
		/*final Message<PloyTypeStat> message1 = timed.receive(2500);
		System.out.println(message1);*/
	}

	
	private PloyParticipator createPloyParticipator() {
		PloyParticipator participator = new PloyParticipator();
		PresentsPloy presentsPloy = new PresentsPloy();
		presentsPloy.setId(1L);
		presentsPloy.setLimitGame("10");
		presentsPloy.setType("W");
		presentsPloy.setPloyName("测试W1");
		
		PresentsPloy presentsPloy1 = new PresentsPloy();
		presentsPloy1.setId(1L);
		presentsPloy1.setLimitGame("10");
		presentsPloy1.setType("W");
		presentsPloy1.setPloyName("测试W2");
		
		List<PresentsPloy> presentsPloys = new ArrayList<PresentsPloy>();
		presentsPloys.add(presentsPloy);
		presentsPloys.add(presentsPloy1);
		return participator;
	}
}
