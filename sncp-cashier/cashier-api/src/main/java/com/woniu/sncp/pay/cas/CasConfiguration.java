package com.woniu.sncp.pay.cas;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CasConfiguration extends WebSecurityConfigurerAdapter{

	@Autowired
    private CasProperties casProperties;

    /*定义认证用户信息获取来源，密码校验规则等*/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider());
    }

    /*定义安全策略*/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()//配置安全策略
                .antMatchers("/","/payment/page").permitAll()//定义/请求不需要验证
                .antMatchers("/security/ttb/pay").authenticated()
                //.anyRequest().authenticated()//其余的所有请求都需要验证
                .and()
                .logout()
                .permitAll()//定义logout不需要验证
                .and()
                .formLogin()//使用form表单登录
                .and()
                .anonymous()
                .authorities("ROLE_ANON");

        http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint())
                .and()
                .addFilter(casAuthenticationFilterForPC())
                .addFilterBefore(casLogoutFilter(), LogoutFilter.class)
                .addFilterBefore(singleSignOutFilter(), AuthenticationFilter.class);
        		
        		//pc  singleSignOutFilter,casAuthenticationFilter,casValidationFilter,casHttpServletRequestWrapperFilter,casAssertionThreadLocalFilter
        
        
        		//m singleSignOutFilter,casAuthenticationFilterForMobile,casValidationFilter,casHttpServletRequestWrapperFilter,casAssertionThreadLocalFilter
        		
        http.csrf().disable(); //禁用CSRF
    }
    
    @Bean
    public AuthenticationFilter casAuthenticationFilterForPC(){
    	AuthenticationFilter casAuthenticationFilterForPC = new org.jasig.cas.client.authentication.AuthenticationFilter();
    	casAuthenticationFilterForPC.setService(casProperties.getAppUrl());
    	casAuthenticationFilterForPC.setCasServerLoginUrl(casProperties.getCasServerLoginUrl());
    	return casAuthenticationFilterForPC;
    }
    
    @Bean
    public AuthenticationFilter casAuthenticationFilterForMobile(){
    	AuthenticationFilter casAuthenticationFilterForMobile = new org.jasig.cas.client.authentication.AuthenticationFilter();
    	casAuthenticationFilterForMobile.setService(casProperties.getAppUrl());
    	casAuthenticationFilterForMobile.setCasServerLoginUrl(casProperties.getCasServerMLoginUrl());
    	return casAuthenticationFilterForMobile;
    }
    
    /*<!-- <!-- CAS Ticket Validation Filter --> -->*/
    @Bean
    public Cas20ProxyReceivingTicketValidationFilter casValidationFilter(){
    	Cas20ProxyReceivingTicketValidationFilter casValidationFilter = new org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter();
    	casValidationFilter.setService(casProperties.getAppUrl());
    	casValidationFilter.setExceptionOnValidationFailure(true);
    	casValidationFilter.setRedirectAfterValidation(true);
    	casValidationFilter.setTicketValidator(cas20ServiceTicketValidator());
    	return casValidationFilter;
    }
    
    /*<!-- CAS HttpServletRequest Wrapper Filter -->*/
    @Bean
    public HttpServletRequestWrapperFilter casHttpServletRequestWrapperFilter(){
    	HttpServletRequestWrapperFilter casHttpServletRequestWrapperFilter = new org.jasig.cas.client.util.HttpServletRequestWrapperFilter();
    	return casHttpServletRequestWrapperFilter;
    }
    
    /*<!-- CAS Assertion Thread Local Filter -->*/
    @Bean
    public AssertionThreadLocalFilter casAssertionThreadLocalFilter(){
    	AssertionThreadLocalFilter casAssertionThreadLocalFilter = new org.jasig.cas.client.util.AssertionThreadLocalFilter();
    	return casAssertionThreadLocalFilter;
    }

    /*认证的入口*/
    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(casProperties.getCasServerLoginUrl());
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    /*指定service相关信息*/
    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casProperties.getAppUrl() + casProperties.getAppLoginUrl());
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    /*CAS认证过滤器*/
    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        casAuthenticationFilter.setFilterProcessesUrl(casProperties.getAppLoginUrl());
        return casAuthenticationFilter;
    }

    /*cas 认证 Provider*/
    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(casUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setKey("casAuthenticationProviderKey");
        return casAuthenticationProvider;
    }

    /*用户自定义的AuthenticationUserDetailsService*/
    @Bean
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> casUserDetailsService() {
    	return new CasUserDetailsService();
    }

    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
        return new Cas20ServiceTicketValidator(casProperties.getCasServerUrl());
    }

    /*单点登出过滤器*/
    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setCasServerUrlPrefix(casProperties.getCasServerUrl());
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    /*请求单点退出过滤器*/
    @Bean
    public LogoutFilter casLogoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter(casProperties.getCasServerLogoutUrl(),
                new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl(casProperties.getAppLogoutUrl());
        return logoutFilter;
}
}
