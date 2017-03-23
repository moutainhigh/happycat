package com.woniu.sncp.pay.core.service.payment.platform.a.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.woniu.sncp.properties.ConfigurableConstants;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
//@ImportResource(locations={"classpath:applicationContext.xml"})
public class XmlCofig extends ConfigurableConstants{

//	private String serverName = "http://cashier.woniu.com";
//	private String loginUrl = "https://sso.woniu.com/login";
//	
//	@Bean
//	public AuthenticationFilter casAuthenticationFilter(){
//		AuthenticationFilter casAuthenticationFilter = new org.jasig.cas.client.authentication.AuthenticationFilter();
//		casAuthenticationFilter.setCasServerLoginUrl("https://sso.woniu.com/login");
//		casAuthenticationFilter.setServerName("http://cashier.woniu.com");
//		return casAuthenticationFilter;
//	}
//	
//	@Bean
//	public AuthenticationFilter casAuthenticationFilterForMobile(){
//		AuthenticationFilter casAuthenticationFilterForMobile = new org.jasig.cas.client.authentication.AuthenticationFilter();
//		casAuthenticationFilterForMobile.setCasServerLoginUrl("https://sso.woniu.com/login");
//		casAuthenticationFilterForMobile.setServerName("http://cashier.woniu.com");
//		return casAuthenticationFilterForMobile;
//	}
//	
//	@Bean
//	public Cas20ServiceTicketValidator ticketValidator(){
//		Cas20ServiceTicketValidator ticketValidator = new org.jasig.cas.client.validation.Cas20ServiceTicketValidator("https://sso.woniu.com/login");
//		return ticketValidator;
//	}
//	@Resource
//	Cas20ServiceTicketValidator ticketValidator;
//	
//	
//	/*<!-- CAS Ticket Validation Filter -->*/
//	@Bean
//	public Cas20ProxyReceivingTicketValidationFilter casValidationFilter(){
//		Cas20ProxyReceivingTicketValidationFilter casValidationFilter = new org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter();
//		casValidationFilter.setServerName("http://cashier.woniu.com");
//		casValidationFilter.setExceptionOnValidationFailure(true);
//		casValidationFilter.setRedirectAfterValidation(true);
//		casValidationFilter.setTicketValidator(ticketValidator);
//		return casValidationFilter;
//	}
//	
//	
//	/*<!-- CAS HttpServletRequest Wrapper Filter -->*/
//	@Bean
//	public HttpServletRequestWrapperFilter casHttpServletRequestWrapperFilter(){
//		HttpServletRequestWrapperFilter casHttpServletRequestWrapperFilter = new org.jasig.cas.client.util.HttpServletRequestWrapperFilter();
//		return casHttpServletRequestWrapperFilter;
//	}
//	/*<!-- CAS Assertion Thread Local Filter -->*/
//	@Bean
//	public AssertionThreadLocalFilter casAssertionThreadLocalFilter(){
//		AssertionThreadLocalFilter casAssertionThreadLocalFilter = new org.jasig.cas.client.util.AssertionThreadLocalFilter();
//		return casAssertionThreadLocalFilter;
//	}
//	/*<!-- CAS Single Sign Out  -->*/
//	@Bean
//	public SingleSignOutFilter singleSignOutFilter(){
//		SingleSignOutFilter singleSignOutFilter = new org.jasig.cas.client.session.SingleSignOutFilter();
//		return singleSignOutFilter;
//	}
}
