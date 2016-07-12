/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: OcpAccountConfig
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
@Configuration
public class OcpAccountConfig {

    @Value("${http.connect.timeout}")
    private int connectTimeout;//连接超时

    @Value("${http.read.timeout}")
    private int readTimeout;//读取超时

    @Value("${http.ocp.account.appid}")
    private String H_APPID;

    @Value("${http.ocp.account.pwd}")
    private String H_PWD;

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
        converters.add( new StringHttpMessageConverter(Charset.forName("UTF-8")) );
        converters.add(new FormHttpMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter());
        return converters;
    }

    @Bean
    public HttpHeaders createOcpAccountHttpHeaders(){
        if( !StringUtils.isEmpty(H_APPID) && !StringUtils.isEmpty(H_PWD) ){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("H_APPID", H_APPID);
            httpHeaders.set("H_PWD", H_PWD);
            return httpHeaders;
        }
        return null;
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
