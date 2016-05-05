package com.woniu.sncp.passport.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Ocp-Passport远程调用配置
 * @author chenyx
 * @date 2016年5月5日
 */
@Configuration
@ConfigurationProperties(prefix = "ocp")
public class OcpPassportRemoteConfig {
	
	/**
	 * 远程调用超时时间
	 */
	private int connectTimeout;
	
	/**
	 * 远程调用返回超时时间
	 */
	private int readTimeout;
	
	/**
	 * ocp远程调用帐号
	 */
	private String appid;
	
	/**
	 * ocp远程调用帐号密码
	 */
	private String pwd;
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
		restTemplate.setMessageConverters(messageConverterList());
		return restTemplate;
	}
	
	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectTimeout(connectTimeout);
		httpRequestFactory.setReadTimeout(readTimeout);
		return httpRequestFactory;
	}
	
	@Bean
	public List<HttpMessageConverter<?>> messageConverterList() {
		List<HttpMessageConverter<?>>  converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new StringHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		converters.add(mappingJackson2HttpMessageConverter());
		return converters;
	}
	
	@Bean
	public HttpHeaders httpHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("H_APPID", appid);
		httpHeaders.set("H_PWD", pwd);
		return httpHeaders;
	}
	
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypeList());
		return mappingJackson2HttpMessageConverter;
	}
	
	public List<MediaType> mediaTypeList() {
		List<MediaType>  mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		return mediaTypes;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
