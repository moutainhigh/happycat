package com.woniu.sncp.pay.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RestTemplateConfig {

	@Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
		
		RestTemplate restTemplate = new RestTemplate(factory);
		restTemplate.setMessageConverters(messageConverters());
//		restTemplate.setErrorHandler(errorHandler);
        return restTemplate;
    }
    
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//    	HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(10000);
        return factory;
    }
    
    public List<HttpMessageConverter<?>> messageConverters() {
    	List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    	
    	messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter());
		messageConverters.add(new ResourceHttpMessageConverter());
		messageConverters.add(new SourceHttpMessageConverter<Source>());
		messageConverters.add(new AllEncompassingFormHttpMessageConverter());
		messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
		
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
				//.featuresToEnable(SerializationFeature.WRAP_ROOT_VALUE, DeserializationFeature.UNWRAP_ROOT_VALUE)
				.build();
		
//		objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(mapper.getTypeFactory()));
		
		messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
//		messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
    	
    	return messageConverters;
    }
}
