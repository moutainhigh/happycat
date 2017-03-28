package com.woniu.sncp.pay;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.snail.ocp.client.http.connection.RestHttpConnection;
import com.snail.ocp.client.http.connection.SpringRestTemplateDelegate;
import com.snail.ocp.client.http.pojo.HttpOption;
import com.snail.ocp.client.pojo.DefaultHeader;
import com.snail.ocp.sdk.http.account.service.AccountInterfaceImpl;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.threadpool.ThreadPool;

import net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean;

/**
 * <p>descrption: 收银台相关配置</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
public class CashierWebAppConfig extends WebMvcConfigurerAdapter{
	
	private static final String ENCODING_CHARSET = "UTF-8";
	
	@Bean(name="characterEncodingFilter")
    public FilterRegistrationBean encodingFilterRegistration() {
		CharacterEncodingFilter encodingFilter = new org.springframework.web.filter.CharacterEncodingFilter();
		encodingFilter.setEncoding(ENCODING_CHARSET);
		encodingFilter.setForceEncoding(true);
		FilterRegistrationBean encodingFilterRegistration = new FilterRegistrationBean(encodingFilter);
		encodingFilterRegistration.addUrlPatterns("/");
        return encodingFilterRegistration;
    }
	
//	@Bean
//    public Filter springSecurityFilterChain() {
//		Filter springSecurityFilterChain = new org.springframework.web.filter.DelegatingFilterProxy();
//////		FilterRegistrationBean springSecurityFilterRegistration = new Filter(springSecurityFilterChain);
//////		springSecurityFilterRegistration.setFilter(springSecurityFilterChain);
//////		springSecurityFilterRegistration.addUrlPatterns("/");
//        return springSecurityFilterChain;
//    }
	
	/**
	 * memcache 配置
	 */
	@Value("${memcached.servers}")
	private String servers;

	@Bean(name={"xmemcachedClient"})
    public XMemcachedClientFactoryBean xmemcachedClient() {
		XMemcachedClientFactoryBean xMemcachedClientFactoryBean = new net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean();
		xMemcachedClientFactoryBean.setServers(servers);
		xMemcachedClientFactoryBean.setFailureMode(true);
        return xMemcachedClientFactoryBean;
    }
	
	/**
	 * OCP 配置
	 */
	@Value("${core.account.app.id}")
	private String appID;
	@Value("${core.account.app.pwd}")
	private String appPWD;
	@Value("${core.account.version}")
	private String version;
	@Value("${core.account.cbc}")
	private String clientBusinessCode;
	
	@Bean(name={"accountHeader"})
	public DefaultHeader getDefaultHeader(){
		DefaultHeader accountHeader = new com.snail.ocp.client.pojo.DefaultHeader();
		accountHeader.setAppID(appID);
		accountHeader.setAppPWD(appPWD);
		accountHeader.setVersion(version);
		accountHeader.setClientBusinessCode(clientBusinessCode);
		return accountHeader;
	}
	
	@Value("${core.account.server}")
	private String server;
	
	@Autowired
	private RestHttpConnection accountRestHttpConnection;
	
	@Bean(name={"httpAccountService"})
	public AccountInterfaceImpl getHttpAccountService(){
		AccountInterfaceImpl httpAccountService = new com.snail.ocp.sdk.http.account.service.AccountInterfaceImpl();
		httpAccountService.setConn(accountRestHttpConnection);
		httpAccountService.setServer(server);
		return httpAccountService;
	}
	
	
	@Bean(name={"accountRestHttpConnection"})
	public RestHttpConnection getAccountRestHttpConnection(){
		RestHttpConnection accountRestHttpConnection = new com.snail.ocp.client.http.connection.RestHttpConnection();
		accountRestHttpConnection.setRestTemplate(accountSpringRestTemplateDelegate);
		return accountRestHttpConnection;
	}
	
	
	@Value("${core.account.connect.timeout}")
	private String connectTimeout;
	@Value("${core.account.read.timeout}")
	private String readTimeout;
	
	@Autowired
	private SpringRestTemplateDelegate accountSpringRestTemplateDelegate;
	
	@Bean(name={"accountSpringRestTemplateDelegate"})
	public SpringRestTemplateDelegate getAccountSpringRestTemplateDelegate(){
		SpringRestTemplateDelegate accountSpringRestTemplateDelegate = new com.snail.ocp.client.http.connection.SpringRestTemplateDelegate();
		HttpOption httpOption = new com.snail.ocp.client.http.pojo.HttpOption();
		httpOption.setConnectTimeout(Integer.parseInt(connectTimeout));
		httpOption.setReadTimeout(Integer.parseInt(readTimeout));
		accountSpringRestTemplateDelegate.setHttpOption(httpOption);
		return accountSpringRestTemplateDelegate;
	}
	
	
	/**
	 * ThreadPool threadPool
	 * */
	@Bean(name={"threadPool"})
	public ThreadPool getThreadPool(){
		ThreadPool threadPool=  new ThreadPool();
		threadPool.setCorePoolSize(5);
		threadPool.setMaximumPoolSize(50);
		threadPool.setKeepAliveTime(8);
		threadPool.setBlockingQueueNum(20);
		return threadPool;
	}
	
	
	
	/**
	 * define Constants 
	 */
	@Bean(name={"paymentConstant"})
	public PaymentConstant getPaymentConstant(){
		PaymentConstant paymentConstant =  new  PaymentConstant();
		paymentConstant.setJdCyberBankMap(jdCyberBankMap);
		paymentConstant.setKqBankCodeMap(kqBankCodeMap);
		paymentConstant.setWebBankMap(webBankMap);
		return paymentConstant;
	}
	
	@Resource
	Map<String,String> kqBankCodeMap;
	
	@Resource
	Map<String,Object> jdCyberBankMap;
	
	@Resource
	Map<String,String> webBankMap;
//	
	@Bean(name={"kqBankCodeMap"})
	public Map<String,String> getKqBankCodeMap(){
		Map<String,String> kqBankCodeMap = new HashMap<String,String>();
		return kqBankCodeMap;
	}
	@Bean(name={"jdCyberBankMap"})
	public Map<String,Object> getJdCyberBankMap(){
		Map<String,Object> jdCyberBankMap = new HashMap<String,Object>();
		return jdCyberBankMap;
	}
	@Bean(name={"webBankMap"})
	public Map<String,String> getWebBankMap(){
		Map<String,String> webBankMap = new HashMap<String,String>();
		return webBankMap;
	}
	
}
