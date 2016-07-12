package com.woniu.sncp.cbss.api.profile.controller;

import java.util.ArrayList;
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
import com.woniu.sncp.profile.dto.CardDetailDTO;
import com.woniu.sncp.profile.dto.CardValueDTO;
import com.woniu.sncp.profile.service.CardTypeManageService;

/**
 * 
 * <p>descrption: 卡类型信息consume</p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class CardTypeManageController {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	
	@Autowired
	private ErrorCode errorCode;
	
	@Autowired
	CardTypeManageService cardTypeManageService;
	
	@RequestMapping(value = "/app/imprest/value", method = RequestMethod.POST)
	@ResponseBody
    public EchoInfo<Object> getCardValueDetail(@RequestBody CardConfRequestDatas requestDatas) {
		CardConfRequestParam data = requestDatas.getParamdata();
		Long gameId = data.getGameId();
		Long paymentId = data.getPlatformId();
		
		//面值大类
		List<CardValueDTO> cardValueDTOList = cardTypeManageService.findValueByGameIdAndPlatformId(gameId, paymentId);
		
		//面值详情
		List<CardDetailDTO> cardDetailDTOList = cardTypeManageService.findDetailByGameIdAndPlatformId(gameId, paymentId);
		
		EchoInfo<Object> retData = null;
		try {
			//返回卡类型对象信息
			retData = errorCode.getErrorCode(1, requestDatas.getSessionId());
			//处理面值大类,将具体面值放入对应的大类
			for(CardValueDTO value:cardValueDTOList){
				List<CardDetailDTO> _cardDetailDTOList = new ArrayList<CardDetailDTO>();
				for(CardDetailDTO detail:cardDetailDTOList){
					if(value.getId().equals(detail.getMainId())){
						_cardDetailDTOList.add(detail);
					}
				}
				value.setDetails(_cardDetailDTOList);
			}
			retData.setData(cardValueDTOList);
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
