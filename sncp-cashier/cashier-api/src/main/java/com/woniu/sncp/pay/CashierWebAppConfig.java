package com.woniu.sncp.pay;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.snail.ocp.client.http.connection.RestHttpConnection;
import com.snail.ocp.client.http.connection.SpringRestTemplateDelegate;
import com.snail.ocp.client.http.pojo.HttpOption;
import com.snail.ocp.client.pojo.DefaultHeader;
import com.snail.ocp.sdk.http.account.service.AccountInterfaceImpl;
import com.woniu.common.memcache.MemcacheCluster;
import com.woniu.kaptcha.servlet.KaptchaServlet;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.core.service.MemcachedService;

import net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean;

/**
 * <p>
 * descrption: 收银台相关配置
 * </p>
 * 
 * @author fuzl
 * @date 2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
public class CashierWebAppConfig extends WebMvcConfigurerAdapter {

	@Bean public MemcacheCluster memcacheCluster(MemcachedService memcachedService) {
		MemcacheCluster  memcacheCluster=	MemcacheCluster.getInstance();
		memcacheCluster.setMemcachedService(memcachedService);
		return memcacheCluster;
	}
	
	@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("locale");
        return lci;
    }
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
    }
	
	/**
	 * 验证码
	 * @return
	 */
	@Bean
	public ServletRegistrationBean captchaServletRegistrationBean() {
		ServletRegistrationBean registration = new ServletRegistrationBean(new KaptchaServlet());
		registration.setEnabled(true);
		registration.addUrlMappings("/Captcha.jpg");
		registration.addInitParameter("kaptcha.border", "no");
		registration.addInitParameter("kaptcha.image.width", "100");
		registration.addInitParameter("kaptcha.image.height", "30");
		registration.addInitParameter("kaptcha.textproducer.font.size", "25");
		registration.addInitParameter("kaptcha.textproducer.char.space", "1");
		registration.addInitParameter("kaptcha.textproducer.font.names", "宋体,宋体");
		registration.addInitParameter("kaptcha.textproducer.char.length", "4");
		registration.addInitParameter("kaptcha.textproducer.char.string", "acde234578gfhjkymnpstuvwx");
		registration.addInitParameter("kaptcha.word.impl", "com.woniu.kaptcha.text.impl.ZHWordRenderer");//<!-- 设置字体不为粗体，默认是粗体 -->
		registration.addInitParameter("kaptcha.textproducer.impl", "com.woniu.kaptcha.text.impl.DefaultTextCreator");//<!-- 设置验证码的文本，如果没有这个param，将显示默认的 -->
		registration.addInitParameter("kaptcha.obscurificator.impl", "com.woniu.kaptcha.impl.ShadowGimpy");//<!-- 设置验证码水印效果，如果没有这个param，将显示默认的 -->
		return registration;
	}
	
	/**
	 * memcache 配置
	 */
	@Value("${memcached.servers}")
	private String servers;

	@Bean(name = { "xmemcachedClient" })
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

	@Bean(name = { "accountHeader" })
	public DefaultHeader getDefaultHeader() {
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

	@Bean(name = { "httpAccountService" })
	public AccountInterfaceImpl getHttpAccountService() {
		AccountInterfaceImpl httpAccountService = new com.snail.ocp.sdk.http.account.service.AccountInterfaceImpl();
		httpAccountService.setConn(accountRestHttpConnection);
		httpAccountService.setServer(server);
		return httpAccountService;
	}

	@Bean(name = { "accountRestHttpConnection" })
	public RestHttpConnection getAccountRestHttpConnection() {
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

	@Bean(name = { "accountSpringRestTemplateDelegate" })
	public SpringRestTemplateDelegate getAccountSpringRestTemplateDelegate() {
		SpringRestTemplateDelegate accountSpringRestTemplateDelegate = new com.snail.ocp.client.http.connection.SpringRestTemplateDelegate();
		HttpOption httpOption = new com.snail.ocp.client.http.pojo.HttpOption();
		httpOption.setConnectTimeout(Integer.parseInt(connectTimeout));
		httpOption.setReadTimeout(Integer.parseInt(readTimeout));
		accountSpringRestTemplateDelegate.setHttpOption(httpOption);
		return accountSpringRestTemplateDelegate;
	}

	/**
	 * define Constants
	 */
	@Bean(name = { "paymentConstant" })
	public PaymentConstant getPaymentConstant() {
		PaymentConstant paymentConstant = new PaymentConstant();
		paymentConstant.setJdCyberBankMap(jdCyberBankMap);
		paymentConstant.setKqBankCodeMap(kqBankCodeMap);
		paymentConstant.setWebBankMap(webBankMap);
		return paymentConstant;
	}

	@Resource
	Map<String, String> kqBankCodeMap;

	@Resource
	Map<String, Object> jdCyberBankMap;

	@Resource
	Map<String, String> webBankMap;

	//<!-- PC快钱快捷支付银行编码映射 -->
	@Bean(name = { "kqBankCodeMap" })
	public Map<String, String> getKqBankCodeMap() {
		Map<String, String> kqBankCodeMap = new HashMap<String, String>();
		kqBankCodeMap.put("ABC","ABC");//<!-- 中国农业银行 -->
		
		kqBankCodeMap.put("COMM","BCOM");//<!-- 交通银行 -->
		kqBankCodeMap.put("BJRCB","BJRCB");//<!-- 北京农村商业银行 -->
		kqBankCodeMap.put("BJBANK","BOB");//<!-- 北京银行 -->
		kqBankCodeMap.put("BOCB2C","BOC");//<!-- 中国银行 -->
		kqBankCodeMap.put("CCB","CCB");//<!-- 中国建设银行 -->
		kqBankCodeMap.put("CEBBANK","CEB");//<!-- 中国光大银行 -->
		kqBankCodeMap.put("CIB","CIB");//<!-- 兴业银行 -->
		kqBankCodeMap.put("CITIC","CITIC");//<!-- 中信银行 -->
		kqBankCodeMap.put("CMB","CMB");//<!-- 招商银行 -->
		kqBankCodeMap.put("CMBC","CMBC");//<!-- 中国民生银行 -->
		
		kqBankCodeMap.put("GDB","GDB");//<!-- 广东发展银行 -->
		kqBankCodeMap.put("HXB","HXB");//<!-- 华夏银行 -->
		kqBankCodeMap.put("HZCBB2C","HZB");//<!-- 杭州银行 -->
		kqBankCodeMap.put("ICBCB2C","ICBC");//<!-- 中国工商银行 -->
		kqBankCodeMap.put("NBBANK","NBCB");//<!-- 宁波银行 -->
		kqBankCodeMap.put("NJCB","NJCB");//<!-- 南京银行 -->
		kqBankCodeMap.put("SPABANK","PAB");//<!-- 平安银行 -->
		kqBankCodeMap.put("POSTGC","PSBC");//<!-- 中国邮政储蓄银行 -->
		kqBankCodeMap.put("SHBANK","SHB");//<!-- 上海银行 -->
		kqBankCodeMap.put("SPDB","SPDB");//<!-- 上海浦东发展银行 -->
		kqBankCodeMap.put("SHRCB","SRCB");//<!-- 上海农商银行 -->
		return kqBankCodeMap;
	}

	//<!-- 京东网银直连 -->
	@Bean(name = { "jdCyberBankMap" })
	public Map<String, Object> getJdCyberBankMap() {
		Map<String, Object> jdCyberBankMap = new HashMap<String, Object>();
		jdCyberBankMap.put("BOCB2C_D","104");//<!-- 中国银行 -->
		jdCyberBankMap.put("ICBCB2C_D","1025");//<!-- 中国工商银行 -->
		jdCyberBankMap.put("CMB_D","3080");//<!-- 招商银行 -->
		jdCyberBankMap.put("CCB_D","1051");//<!-- 中国建设银行 -->
		jdCyberBankMap.put("ABC_D","103");//<!-- 中国农业银行 -->
		jdCyberBankMap.put("SPDB_D","314");//<!-- 上海浦东发展银行 -->
		jdCyberBankMap.put("CIB_D","309");//<!-- 兴业银行 -->
		
		jdCyberBankMap.put("GDB_D","3061");//<!-- 广东发展银行 -->
		jdCyberBankMap.put("CMBC_D","305");//<!-- 中国民生银行 -->
		jdCyberBankMap.put("COMM_D","301");//<!-- 交通银行 -->
		jdCyberBankMap.put("CITIC_D","313");//<!-- 中信银行 -->
		jdCyberBankMap.put("HZCBB2C_D","324");//<!-- 杭州银行 -->
		jdCyberBankMap.put("CEBBANK_D","312");//<!-- 中国光大银行 -->
		jdCyberBankMap.put("SHBANK_D","326");//<!-- 上海银行 -->
		jdCyberBankMap.put("NBBANK_D","302");//<!-- 宁波银行 -->
		jdCyberBankMap.put("SPABANK_D","307");//<!-- 平安银行 -->
		jdCyberBankMap.put("BJRCB_D","335");//<!-- 北京农村商业银行 -->
		jdCyberBankMap.put("POSTGC_D","3230");//<!-- 中国邮政储蓄银行 -->
		jdCyberBankMap.put("BJBANK_D","310");//<!-- 北京银行 -->
		jdCyberBankMap.put("BOCD_D","336");//<!-- 成都银行 -->
		jdCyberBankMap.put("QDCCB_D","3341");//<!-- 青岛银行 -->
		jdCyberBankMap.put("NJCB_D","316");//<!-- 南京银行 -->
		jdCyberBankMap.put("HXB_D","311");//<!-- 华夏银行 -->
		jdCyberBankMap.put("CQRCB_D","342");//<!-- 重庆农村商业银行 -->
		jdCyberBankMap.put("SHRCB_D","343");//<!-- 上海农商银行 -->
		jdCyberBankMap.put("EGBK_D","344");//<!-- 恒丰银行 -->
		jdCyberBankMap.put("BOCB2C_C","106");//<!-- 中国银行 -->
		jdCyberBankMap.put("ICBCB2C_C","1027");//<!-- 中国工商银行 -->
		jdCyberBankMap.put("CMB_C","308");//<!-- 招商银行 -->
		jdCyberBankMap.put("CCB_C","1054");//<!-- 中国建设银行 -->
		jdCyberBankMap.put("ABC_C","1031");//<!-- 中国农业银行 -->
		jdCyberBankMap.put("SPDB_C","3141");//<!-- 上海浦东发展银行 -->
		jdCyberBankMap.put("CIB_C","3091");//<!-- 兴业银行 -->
		jdCyberBankMap.put("GDB_C","306");//<!-- 广东发展银行 -->
		jdCyberBankMap.put("CMBC_C","3051");//<!-- 中国民生银行 -->
		jdCyberBankMap.put("COMM_C","301");//<!-- 交通银行 -->
		jdCyberBankMap.put("CITIC_C","3131");//<!-- 中信银行 -->
		jdCyberBankMap.put("HZCBB2C_C","3241");//<!-- 杭州银行 -->
		jdCyberBankMap.put("CEBBANK_C","3121");//<!-- 中国光大银行 -->
		jdCyberBankMap.put("SHBANK_C","3261");//<!-- 上海银行 -->
		jdCyberBankMap.put("NBBANK_C","303");//<!-- 宁波银行 -->
		jdCyberBankMap.put("SPABANK_C","3071");//<!-- 平安银行 -->
		jdCyberBankMap.put("POSTGC_C","3231");//<!-- 中国邮政储蓄银行 -->
		jdCyberBankMap.put("QDCCB_C","334");//<!-- 青岛银行 -->
		jdCyberBankMap.put("HXB_C","3112");//<!-- 华夏银行 -->
		return jdCyberBankMap;
	}

	//网银直充
	@Bean(name = { "webBankMap" })
	public Map<String, String> getWebBankMap() {
		Map<String, String> webBankMap = new HashMap<String, String>();
		webBankMap.put("ICBCB2C","a");
		webBankMap.put("ABC","b");
		webBankMap.put("BOCB2C","c");
		webBankMap.put("CCB","d");
		webBankMap.put("CMB","e");
		webBankMap.put("SPDB","f");
		webBankMap.put("COMM","g");
		webBankMap.put("CITIC","h");
		webBankMap.put("HZCBB2C","i");
		webBankMap.put("CEBBANK","j");
		webBankMap.put("CIB","k");
		webBankMap.put("GDB","l");
		webBankMap.put("SDB","m");
		webBankMap.put("CMBC","n");
		webBankMap.put("SHBANK","o");
		webBankMap.put("NBBANK","p");
		webBankMap.put("SPABANK","q");
		webBankMap.put("BJRCB","r");
		webBankMap.put("FDB","s");
		webBankMap.put("POSTGC","G");
		webBankMap.put("WZCBB2C-DEBIT","p");
		return webBankMap;
	}

	
	/**
	 * 国际化加载
	 */
	@Value(value = "${spring.messages.basename}")
    private String basename;

    @Bean(name = "messageSource")
    public ResourceBundleMessageSource getMessageResource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(basename);
        return messageSource;
    }
    
    @Bean(name={"moneyBookersMethodMap"})
	public Map<String, String> getMoneyBookersMethodMap(){
		Map<String,String> moneyBookersMethodMap = new HashMap<String,String>();
		moneyBookersMethodMap.put("WLT", "WLT");
		moneyBookersMethodMap.put("VSA", "VSA");
		moneyBookersMethodMap.put("MSC", "MSC");
		moneyBookersMethodMap.put("AMX", "AMX");
		moneyBookersMethodMap.put("JCB", "JCB");
		moneyBookersMethodMap.put("MAE", "MAE");
		moneyBookersMethodMap.put("DIN", "DIN");
		return moneyBookersMethodMap;
	}
    
    @Value(value = "${paypal.mode}")
	private String paypalMode;
	@Value(value = "${paypal.clientId}")
	private String paypalClientId;
	@Value(value = "${paypal.clientSecret}")
	private String clientSecret;
	
	@Value(value = "${paypal.acct1.UserName}")
	private String paypalUserName;
	
	@Value(value = "${paypal.acct1.Password}")
	private String paypalPassword;
	
	@Value(value = "${paypal.acct1.Signature}")
	private String paypalSignature;
	
	@Bean(name={"paypalConfigurationMap"})
	public Map<String, String> getPaypalConfigurationMap(){
		Map<String,String> paypalConfigurationMap = new HashMap<String,String>();
		paypalConfigurationMap.put("mode", paypalMode);//<!-- Endpoints are varied depending on whether sandbox OR live is chosen for mode -->
		paypalConfigurationMap.put("sandbox.EmailAddress", "paypalsnail@snailgame.net");
		paypalConfigurationMap.put("clientId", paypalClientId);//<!-- Credentials -->
		paypalConfigurationMap.put("clientSecret", clientSecret);
		paypalConfigurationMap.put("http.ConnectionTimeOut", "10000");//<!-- Connection Information -->
		paypalConfigurationMap.put("http.Retry", "2");
		paypalConfigurationMap.put("http.ReadTimeOut", "30000");
		paypalConfigurationMap.put("http.MaxConnection", "100");
		paypalConfigurationMap.put("http.GoogleAppEngine", "false");//<!-- Set this property to true if you are using the PayPal SDK within a Google App Engine java app -->
		paypalConfigurationMap.put("acct1.UserName", paypalUserName);//<!-- Account Credential -->
		paypalConfigurationMap.put("acct1.Password", paypalPassword);
		paypalConfigurationMap.put("acct1.Signature", paypalSignature);
		return paypalConfigurationMap;
	}
	
	@Bean(name="paypalExecutor")
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("Paypal-");
		executor.initialize();
		return executor;
	}
}
