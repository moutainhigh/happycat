package com.woniu.sncp.alarm.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.woniu.sncp.alarm.dto.AlarmMessageTo;
import com.woniu.sncp.exception.SystemException;

public class AlarmMessageServiceImpl implements AlarmMessageService {

	private Logger logger = LoggerFactory.getLogger(AlarmMessageServiceImpl.class);  
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * 服务地址
	 */
	@Value("${alarm.url}")
	private String alarmUrl;
	
	@Override
	public void sendMessage(AlarmMessageTo alarmMessage) {
		if(alarmMessage == null 
				|| StringUtils.isBlank(alarmMessage.getSrc())
				|| StringUtils.isBlank(alarmMessage.getContent()))
			return ;
		
		MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<String, String>();
		bodyMap.add("ctime", String.valueOf((new Date()).getTime()/1000));
		bodyMap.add("src", alarmMessage.getSrc());
		bodyMap.add("content", alarmMessage.getContent());
		
		try {
			logger.info("request: " + alarmUrl  + ","  + bodyMap);
			ResponseEntity<String> response = restTemplate.postForEntity(alarmUrl, bodyMap,String.class);
			logger.info("response: " + alarmUrl + ","  + response);
		} catch(ResourceAccessException rae) {
			throw new SystemException("ResourceAccessException：" + rae.getMessage());
		} catch(Exception e) {
			throw new SystemException("Other Exception " + e.getMessage());
		}
	}

}
