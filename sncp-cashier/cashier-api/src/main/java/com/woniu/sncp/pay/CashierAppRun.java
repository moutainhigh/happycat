package com.woniu.sncp.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//"com.woniu.sncp.pay.core",
@ComponentScan(basePackages={"com.woniu.sncp.pojo","com.woniu.sncp.pay","com.woniu.sncp.ocp.business.passport"})
@SpringBootApplication
@EnableAutoConfiguration
public class CashierAppRun {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 SpringApplication.run(CashierAppRun.class, args);
	}
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {

	   return (container -> {
	        
		    ErrorPage error400Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/400");
		    ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401");
	        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
	        ErrorPage error405Page = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/405");
	        ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500");

	        container.addErrorPages(error400Page,error401Page, error404Page, error405Page,error500Page);
	   });
	}
//	@Bean
//    public PaymentMerchantService getService() {
//    	return new PaymentMerchantService();
//    }
	
}
