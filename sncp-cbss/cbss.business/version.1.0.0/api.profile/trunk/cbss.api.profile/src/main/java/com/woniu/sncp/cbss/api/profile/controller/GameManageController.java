package com.woniu.sncp.cbss.api.profile.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.cbss.api.profile.response.GameAreaResponseData;
import com.woniu.sncp.cbss.api.profile.response.GameAreaResponseDatas;
import com.woniu.sncp.cbss.api.profile.response.GameAreasResponseDatas;
import com.woniu.sncp.cbss.core.authorize.AccessAuthorizeFilterConfigures;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.profile.dto.GameAreaDTO;
import com.woniu.sncp.profile.dto.GameGroupDTO;
import com.woniu.sncp.profile.service.GameManageService;

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
		EchoInfo<Object> retData = null;
		try {
			DozerBeanMapper beanMapper = new DozerBeanMapper();
			List<GameAreasResponseDatas> gameAreasResponseDatasList = new ArrayList<GameAreasResponseDatas>(gameGroupDTOList.size());
			if(null!=gameGroupDTOList&&gameGroupDTOList.size()>0){
				for(GameGroupDTO groupDto:gameGroupDTOList){
					//组装响应信息
					//网络类型
					GameAreasResponseDatas gameAreasResponseDatas = new GameAreasResponseDatas();
					gameAreasResponseDatas.setGameId(groupDto.getGameId());
					gameAreasResponseDatas.setGroupId(groupDto.getId());
					gameAreasResponseDatas.setGroupName(groupDto.getGroupName());
					gameAreasResponseDatas.setSequence(groupDto.getSequence());
					Set<GameAreaDTO> gameAreaDtoSet = groupDto.getGameAreaSet();
					Set<GameAreaResponseDatas> gameAreaResponseDatasSet = new HashSet<GameAreaResponseDatas>(gameAreaDtoSet.size());
					for(GameAreaDTO areaDto:gameAreaDtoSet){
						GameAreaResponseDatas gameAreaResponseDatas= new GameAreaResponseDatas();
						beanMapper.map(areaDto, gameAreaResponseDatas);
						gameAreaResponseDatasSet.add(gameAreaResponseDatas);
					}
					//分区信息
					gameAreasResponseDatas.setAreas(gameAreaResponseDatasSet);
					
					gameAreasResponseDatasList.add(gameAreasResponseDatas);
				}
			}
			
			retData = errorCode.getErrorCode(1, requestDatas.getSessionId());
			retData.setData(gameAreasResponseDatasList);
		} catch (MissingParamsException e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("gameConf", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
		
		return retData;
	}
	
	@RequestMapping(value = "/app/imprest/area", method = RequestMethod.POST)
	@ResponseBody
    public EchoInfo<Object> getGameAreaNotImprestType(@RequestBody GameConfRequestDatas requestDatas) {
		GameConfRequestParam data = requestDatas.getParamdata();
		Long serverId = data.getServerId();
		GameAreaDTO gameAreaDTO = gameManageService.findByServerId(serverId);
		EchoInfo<Object> retData = null;
		try {
			GameAreaResponseData gameAreaResponseData = new GameAreaResponseData();
			gameAreaResponseData.setAreaId(gameAreaDTO.getId());
			gameAreaResponseData.setAreaName(gameAreaDTO.getName());
			retData = errorCode.getErrorCode(1, requestDatas.getSessionId());
			retData.setData(gameAreaResponseData);
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
