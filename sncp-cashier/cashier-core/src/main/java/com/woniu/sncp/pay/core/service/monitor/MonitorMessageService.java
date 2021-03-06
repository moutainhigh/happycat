package com.woniu.sncp.pay.core.service.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;

@Service
public class MonitorMessageService{
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String,Object> bodyMap = new HashMap<String,Object>();
	
	@Value("${monitor.type}")
	private String src;
	
	@Value("${monitor.url}")
	private String monitorUrl;
	
	@Value("${monitor.timeout}")
	private String monitorTimeout;
	
	@PostConstruct
	public void init(){
		bodyMap.put("src", src);
	}
	
	@Async
	public void sendMsg(String message) {
		try{
			bodyMap.put("content", message);
			bodyMap.put("ctime", (new Date()).getTime()/1000);
			String postBodyRequst = PayCheckUtils.postRequst(monitorUrl, bodyMap, Integer.valueOf(monitorTimeout), "utf-8", "MonitorMessageTask");
			logger.info("monitor-send-response "+postBodyRequst);
			
			String code = String.valueOf(JsonUtils.jsonToMap(postBodyRequst).get("code"));
			if(!"1".equals(code)){
				logger.error("monitor-send-error " + bodyMap);
			} 
		} catch (Exception e){
			logger.error("monitor-send-exception " + e.getMessage(),e);
		}
	}
	
}
