package com.snail.stream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.snail.stream.send.SendingBean;

@RestController
public class MessageController {
	
	@Autowired
	private SendingBean sendingBean;

	@RequestMapping("/send")
	public String send() {
		sendingBean.sayHello("hello");
		return "success";
	}
}
