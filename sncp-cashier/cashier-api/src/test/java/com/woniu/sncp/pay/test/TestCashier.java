package com.woniu.sncp.pay.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pojo.payment.PaymentOrder;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(com.woniu.sncp.pay.CashierApplication.class)
public class TestCashier {
	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
//	public static void main(String[] args) throws UnsupportedEncodingException {
//		// TODO Auto-generated method stub
////		 SpringApplication.run(TestCashier.class, args);
////		String orderNo = "20170331-3003-007-0002749840";
////		String seq = orderNo.substring(orderNo.lastIndexOf("-"));
////		System.out.println(seq);
//		
////		System.out.println(URLEncoder.encode("区  服","utf-8"));
//	}

//	@Autowired
//	PaymentOrderService paymentOrderService;
//	
//	@Test
//	public void testAspectj(){
//		
//		PaymentOrder order = paymentOrderService.queryOrder("20170411-1039-007-0000000079");
//		System.out.println(order.getBody());
//	}
	
	@Autowired
	BaseSessionDAO sessionDAO;

	@Value(value = "${spring.db.machine-name}")
    private String machineName;
	
	@Test
	public void queryMachineState(){
		StringBuffer queryString = new StringBuffer("SELECT * FROM performance_schema.replication_group_members ");
		if(StringUtils.isNotBlank(machineName)){
			queryString.append(" where MEMBER_HOST = '"+ machineName +"'");
		}
		
		List<Map<String, Object>> maps = null;
		try {
			maps = sessionDAO.jdbcList(queryString.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null != maps && maps.size()>0){
			Map<String, Object> map = maps.get(0);
			System.out.println("当前机器名称:"+map.get("MEMBER_HOST"));
			System.out.println("当前机器状态:"+map.get("MEMBER_STATE"));
			
//			for(Map<String, Object> map:maps){
//				Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
//				while(iter.hasNext()){
//					Map.Entry<String, Object> entry = iter.next();
//					if(entry.getValue().equals(machineName)){
//						System.out.println("当前机器状态:"+map.get("MEMBER_STATE"));
//					}
//					System.out.println(entry.getKey() + "=" + entry.getValue());
//				}
//			}
		}
		
	}
}
