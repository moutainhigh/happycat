package com.woniu.sncp.cbss.api.profile.controller;

import java.util.HashMap;
import java.util.List;
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
import com.woniu.sncp.profile.dto.CardDetailDTO;
import com.woniu.sncp.profile.dto.CardValueDTO;
import com.woniu.sncp.profile.service.CardManageService;

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
public class CardManageController {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	
	@Autowired
	private ErrorCode errorCode;
	
	@Autowired
	CardManageService cardManageService;
	
	@RequestMapping(value = "/app/imprest/value", method = RequestMethod.POST)
	@ResponseBody
    public EchoInfo<Object> getCardValueDetail(@RequestBody CardConfRequestDatas requestDatas) {
		CardConfRequestParam data = requestDatas.getParamdata();
		Long gameId = data.getGameId();
		Long paymentId = data.getPlatformId();
		
		//面值大类
		List<CardValueDTO> cardValueDTOList = cardManageService.findValueByGameIdAndPlatformId(gameId, paymentId);
		
		//面值详情
		List<CardDetailDTO> cardDetailDTOList = cardManageService.findDetailByGameIdAndPlatformId(gameId, paymentId);
		
		EchoInfo<Object> retData = null;
		try {
			Map<String,Object> retMap = new HashMap<String,Object>();
			retData = errorCode.getErrorCode(1, requestDatas.getSessionId());
			retMap.put("value", cardValueDTOList);
			retMap.put("detail", cardDetailDTOList);
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
