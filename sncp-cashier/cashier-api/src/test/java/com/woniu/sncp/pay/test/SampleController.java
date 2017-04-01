package com.woniu.sncp.pay.test;

import java.util.Calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@Controller
//@EnableAutoConfiguration
public class SampleController {

//	@RequestMapping("/hello")
//    @ResponseBody
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
//        SpringApplication.run(SampleController.class, args);
    	int[] month = new int[]{1,2,3,4,5,6,7,8,9,10,11,12};
    	int[] days = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
    	String inCompany = "2014年8月18日";
    	
    }
}
