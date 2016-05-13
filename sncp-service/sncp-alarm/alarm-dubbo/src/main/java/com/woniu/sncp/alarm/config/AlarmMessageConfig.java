package com.woniu.sncp.alarm.config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author luzz
 *
 */
@Configuration
public class AlarmMessageConfig {
	
	@Value("${http.connect.timeout}")
	private int connectTimeout;//连接超时
	
	@Value("${http.read.timeout}")
	private int readTimeout;//读取超时
	
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
	
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypeList());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
		mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
		return mappingJackson2HttpMessageConverter;
	}
	
	public List<MediaType> mediaTypeList() {
		List<MediaType>  mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		return mediaTypes;
	}
}
