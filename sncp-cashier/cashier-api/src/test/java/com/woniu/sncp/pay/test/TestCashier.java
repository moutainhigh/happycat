package com.woniu.sncp.pay.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@SpringBootApplication(scanBasePackages = { 
//		"com.woniu.sncp.pay.test" })
public class TestCashier {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		 SpringApplication.run(TestCashier.class, args);
		String orderNo = "20170331-3003-007-0002749840";
		String seq = orderNo.substring(orderNo.lastIndexOf("-"));
		System.out.println(seq);
	}

}
