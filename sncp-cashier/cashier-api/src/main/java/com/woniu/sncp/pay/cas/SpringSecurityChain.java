package com.woniu.sncp.pay.cas;

import javax.annotation.Resource;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.woniu.sncp.pay.core.filter.AuthenticationCommonFilter;
import com.woniu.sncp.pay.core.filter.LogMonitorFilter;
import com.woniu.sncp.pay.core.filter.RequestClearFilter;

/**
 * <p>
 * descrption:
 * </p>
 * 
 * @author fuzl
 * @date 2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityChain extends WebSecurityConfigurerAdapter{
	@Autowired
	SecurityCasConfigure autoconfig;

	private static boolean casEnabled = true;
	
	public SpringSecurityChain() {
	}

	/*定义安全策略*/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()//配置安全策略
//                .antMatchers("/").permitAll()//定义/请求不需要验证
//                //.antMatchers("/security/ttb/pay").authenticated()
//                //.anyRequest().authenticated()//其余的所有请求都需要验证
//                .and()
//                .logout()
//                .permitAll()//定义logout不需要验证
//                //.and()
//                //.formLogin()//使用form表单登录
//                .addObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
//    				public <O extends FilterSecurityInterceptor> O postProcess(
//    						O fsi) {
//    					fsi.setPublishAuthorizationSuccess(true);
//    					return fsi;
//    				}
//    			});

//        http.exceptionHandling()
//                .and()
//                .addFilter(filter)
//                .addFilterBefore(casLogoutFilter(), LogoutFilter.class)
//                .addFilterBefore(singleSignOutFilter(), AuthenticationFilter.class);
        		
        		//pc  singleSignOutFilter,casAuthenticationFilter,casValidationFilter,casHttpServletRequestWrapperFilter,casAssertionThreadLocalFilter
        
        
        		//m singleSignOutFilter,casAuthenticationFilterForMobile,casValidationFilter,casHttpServletRequestWrapperFilter,casAssertionThreadLocalFilter
        		
        http.csrf().disable(); //禁用CSRF
    }
	
	@Bean
	public SecurityCasConfigure getSpringCasAutoconfig() {
		return new SecurityCasConfigure();
	}

	/**
	 * 用于实现单点登出功能
	 */
	@Bean
	public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {
		ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listener = new ServletListenerRegistrationBean<>();
		listener.setEnabled(casEnabled);
		listener.setListener(new SingleSignOutHttpSessionListener());
		listener.setOrder(1);
		return listener;
	}

	/**
	 * 该过滤器用于实现单点登出功能，单点退出配置，一定要放在其他filter之前
	 */
	@Bean
	public FilterRegistrationBean logOutFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		LogoutFilter logoutFilter = new LogoutFilter(
				autoconfig.getCasServerUrlPrefix() + "/logout?service=" + autoconfig.getServerName(),
				new SecurityContextLogoutHandler());
		filterRegistration.setFilter(logoutFilter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/logout");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(2);
		return filterRegistration;
	}

	/**
	 * 该过滤器用于实现单点登出功能，单点退出配置，一定要放在其他filter之前
	 */
	@Bean
	public FilterRegistrationBean singleSignOutFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new SingleSignOutFilter());
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(3);
		return filterRegistration;
	}

	/**
	 * 该过滤器负责用户的认证工作
	 */
	@Bean
	public FilterRegistrationBean authenticationFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new AuthenticationFilter());
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getAuthFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAuthFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		// casServerLoginUrl:cas服务的登陆url
		filterRegistration.addInitParameter("casServerLoginUrl", autoconfig.getCasServerLoginUrl());
		// 本项目登录ip+port
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.addInitParameter("useSession", autoconfig.isUseSession() ? "true" : "false");
		filterRegistration.addInitParameter("redirectAfterValidation",
				autoconfig.isRedirectAfterValidation() ? "true" : "false");
		filterRegistration.setOrder(4);
		return filterRegistration;
	}

	/**
	 * 该过滤器负责对Ticket的校验工作
	 */
	@Bean
	public FilterRegistrationBean cas20ProxyReceivingTicketValidationFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		Cas20ProxyReceivingTicketValidationFilter cas20ProxyReceivingTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
		// cas20ProxyReceivingTicketValidationFilter.setTicketValidator(cas20ServiceTicketValidator());
		cas20ProxyReceivingTicketValidationFilter.setServerName(autoconfig.getServerName());
		filterRegistration.setFilter(cas20ProxyReceivingTicketValidationFilter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getValidateFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getValidateFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(5);
		return filterRegistration;
	}

	/**
	 * 该过滤器对HttpServletRequest请求包装，
	 * 可通过HttpServletRequest的getRemoteUser()方法获得登录用户的登录名
	 * 
	 */
	@Bean
	public FilterRegistrationBean httpServletRequestWrapperFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new HttpServletRequestWrapperFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getRequestWrapperFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getRequestWrapperFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.setOrder(6);
		return filterRegistration;
	}

	/**
	 * 该过滤器使得可以通过org.jasig.cas.client.util.AssertionHolder来获取用户的登录名。
	 * 比如AssertionHolder.getAssertion().getPrincipal().getName()。
	 * 这个类把Assertion信息放在ThreadLocal变量中，这样应用程序不在web层也能够获取到当前登录信息
	 */
	@Bean
	public FilterRegistrationBean assertionThreadLocalFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new AssertionThreadLocalFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getAssertionFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAssertionFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.setOrder(7);
		return filterRegistration;
	}
	
	
	
	
	/**
	 * 自定义过滤器，包括通用验证、日志格式化输出、requestClear。
	 */
	@Resource
	AuthenticationCommonFilter authenticationCommonFilter;
	
	@Bean
	public FilterRegistrationBean authenticationCommonFilterBean() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.addUrlPatterns("/api/refundment/refund/*");
		filterRegistration.addUrlPatterns("/payment/trans/*");
		filterRegistration.addUrlPatterns("/api/excharge/order/*");
		filterRegistration.addUrlPatterns("/fcb/pay/**");
		filterRegistration.addUrlPatterns("/fcb/pay");
		filterRegistration.addUrlPatterns("/cancel/api/json");
		filterRegistration.addUrlPatterns("/payment/api/*");
		filterRegistration.addUrlPatterns("/payment/api");
		filterRegistration.addUrlPatterns("/wap/api/*");
		filterRegistration.addUrlPatterns("/payment/api/jsonp");
		filterRegistration.addUrlPatterns("/payment/api/dp/json");
		filterRegistration.addUrlPatterns("/security/ttb/pay");
		filterRegistration.addUrlPatterns("/api/tgt/ttb/pay/json");
		filterRegistration.addUrlPatterns("/security/ttb/pay/json");
		filterRegistration.addUrlPatterns("/wap/api/security/ttb/pay/json");
	    filterRegistration.setFilter(authenticationCommonFilter);
	    filterRegistration.setOrder(8);
		return filterRegistration;
	}
	
	@Resource
	LogMonitorFilter logMonitorFilter;
	
	@Bean
	public FilterRegistrationBean logMonitorFilterBean() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.addUrlPatterns("/api/refundment/refund/*");
		filterRegistration.addUrlPatterns("/payment/trans/*");
		filterRegistration.addUrlPatterns("/api/excharge/order/*");
		filterRegistration.addUrlPatterns("/fcb/pay/**");
		filterRegistration.addUrlPatterns("/fcb/pay");
		filterRegistration.addUrlPatterns("/cancel/api/json");
		filterRegistration.addUrlPatterns("/payment/api/*");
		filterRegistration.addUrlPatterns("/payment/api");
		filterRegistration.addUrlPatterns("/wap/api/*");
		filterRegistration.addUrlPatterns("/payment/api/jsonp");
		filterRegistration.addUrlPatterns("/payment/api/dp/json");
		filterRegistration.addUrlPatterns("/security/ttb/pay");
		filterRegistration.addUrlPatterns("/api/tgt/ttb/pay/json");
		filterRegistration.addUrlPatterns("/security/ttb/pay/json");
		filterRegistration.addUrlPatterns("/wap/api/security/ttb/pay/json");
		filterRegistration.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		filterRegistration.setFilter(logMonitorFilter);
	    filterRegistration.setOrder(9);
		return filterRegistration;
	}
	
	@Autowired
	RequestClearFilter requestClearFilter;
	@Bean
	public FilterRegistrationBean requestClearFilterRegistration() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.addUrlPatterns("/payment/backend/api/common/**");
		filterRegistration.setFilter(requestClearFilter);
		filterRegistration.setOrder(10);
		return filterRegistration;
	}
	
	@Value("spring.druid.white.ips")
	private String whiteIps;
	@Value("spring.druid.black.ips")
	private String blackIps;
	@Value("spring.druid.username")
	private String username;
	@Value("spring.druid.pwd")
	private String pwd;
	/**
	 * 
	 * 注册一个StatViewServlet
	 * @return
	 */
	@Bean
	public ServletRegistrationBean DruidStatViewServlet() {
		// org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
		ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),
				"/druid/*");
		// 添加初始化参数：initParams
		// 白名单：
		servletRegistrationBean.addInitParameter("allow", whiteIps);
		// IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not
		// permitted to view this page.
		servletRegistrationBean.addInitParameter("deny", blackIps);
		// 登录查看信息的账号密码.
		servletRegistrationBean.addInitParameter("loginUsername", username);
		servletRegistrationBean.addInitParameter("loginPassword", pwd);

		// 是否能够重置数据.
		servletRegistrationBean.addInitParameter("resetEnable", "false");
		return servletRegistrationBean;

	}

	/**
	 * 
	 * 注册一个：filterRegistrationBean
	 * @return
	 */
	@Bean(name="stat")
	public FilterRegistrationBean druidStatFilter() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
		// 添加过滤规则.
		filterRegistrationBean.addUrlPatterns("/*");
		// 添加不需要忽略的格式信息.
		filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		filterRegistrationBean.setOrder(11);
		return filterRegistrationBean;

	}
}
