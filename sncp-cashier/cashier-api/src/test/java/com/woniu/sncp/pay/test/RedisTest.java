package com.woniu.sncp.pay.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import redis.clients.jedis.JedisCluster;

/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年4月1日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(com.woniu.sncp.pay.CashierApplication.class)
public class RedisTest {

	// @Autowired
	private JedisCluster jedisCluster;

	// @Test
	public void get() {
		jedisCluster.set("youqian-spread-sync-to-mysql-date", "" + System.currentTimeMillis());
		System.out.println("==============" + jedisCluster.get("youqian-spread-sync-to-mysql-date"));
	}

	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void page() {
//		String queryString = "select * from SN_PAY.PAY_ORDER";
//		List<PaymentOrder> orderList =  (List<PaymentOrder>) sessionDAO.page(queryString, null, 6, 3, "N_ORDER_ID", true,PaymentOrder.class);
//	     
//		for(PaymentOrder order:orderList){
//			System.out.println(order.getOrderId()+","+order.getMoney()+","+order.getOrderNo());
//		}
//	}

}
