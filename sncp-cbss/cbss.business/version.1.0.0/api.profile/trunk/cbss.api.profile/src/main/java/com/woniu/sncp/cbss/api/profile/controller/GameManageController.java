package com.woniu.sncp.cbss.api.profile.controller;

import java.util.List;

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
import com.woniu.sncp.profile.dto.GameGroupDTO;
import com.woniu.sncp.profile.service.GameManageService;

/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016Äê7ÔÂ1ÈÕ
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class GameManageController {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	
	@Autowired
	private ErrorCode errorCode;
	
	@Autowired
	GameManageService gameManageService;
	
	@RequestMapping(value = "/app/imprest/areas", method = RequestMethod.POST)
	@ResponseBody
    public EchoInfo<Object> getGameAreaListNotImprestType(@RequestBody GameConfRequestDatas requestDatas) {
		GameConfRequestParam data = requestDatas.getParamdata();
		Long gameId = data.getGameId();
		String state = data.getState();
		String type = data.getType();
		List<GameGroupDTO> gameGroupDTOList = gameManageService.findByGameIdAndStateAndType(gameId, state, type);
		try {
			
		} catch (MissingParamsException e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
		
		return null;
	}
}
