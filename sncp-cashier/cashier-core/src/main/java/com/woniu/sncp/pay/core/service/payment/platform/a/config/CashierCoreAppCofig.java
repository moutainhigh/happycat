package com.woniu.sncp.pay.core.service.payment.platform.a.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.snail.ocp.client.http.connection.RestHttpConnection;
import com.snail.ocp.client.http.connection.SpringRestTemplateDelegate;
import com.snail.ocp.client.http.pojo.HttpOption;
import com.snail.ocp.client.pojo.DefaultHeader;
import com.snail.ocp.sdk.http.account.service.AccountInterfaceImpl;
import com.woniu.sncp.pay.common.threadpool.ThreadPool;
import com.woniu.sncp.pay.common.utils.PaymentConstant;

import net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@Configuration
//@ImportResource(locations={"classpath:applicationContext.xml"})
public class CashierCoreAppCofig {
	
}
