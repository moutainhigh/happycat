package com.woniu.sncp.cbss.api.profile.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.cbss.core.authorize.AccessAuthorizeFilterConfigures;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.profile.service.ActivityManageService;

/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class ActivityManageController {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	
	@Autowired
	private ErrorCode errorCode;
	
	@Autowired
	ActivityManageService activityManageService;
	
	@RequestMapping(value = "/app/imprest/allactivity", method = RequestMethod.POST)
	@ResponseBody
    public EchoInfo<Object> getAllActivity(@RequestBody ActivityConfRequestDatas requestDatas) {
		ActivityConfRequestParam data = requestDatas.getParamdata();
		Long gameId = data.getGameId();
		String state = data.getState();
		
		Map<String,Object> retMap = activityManageService.findAllPloysByState(gameId, state);
		EchoInfo<Object> retData = null;
		try {
			retData = errorCode.getErrorCode(1, requestDatas.getSessionId());
			retData.setData(retMap);
		} catch (MissingParamsException e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
		
		return retData;
	}
	
}
