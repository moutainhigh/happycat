package com.woniu.sncp.profile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.profile.dto.GameAreaDTO;
import com.woniu.sncp.profile.dto.GameGroupDTO;
import com.woniu.sncp.profile.exception.GameAreaNotFoundException;
import com.woniu.sncp.profile.exception.GameGroupNotFoundException;
import com.woniu.sncp.profile.exception.GameServerNotFoundException;
import com.woniu.sncp.profile.jpa.GameAreaRepository;
import com.woniu.sncp.profile.jpa.GameGroupRepository;
import com.woniu.sncp.profile.jpa.GameServerRepository;
import com.woniu.sncp.profile.po.GameAreaPo;
import com.woniu.sncp.profile.po.GameGroupPo;
import com.woniu.sncp.profile.po.GameServerPo;

/**
 * 
 * <p>descrption: 游戏网络类型、分区信息管理</p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class GameManageServiceImpl implements GameManageService {

	protected static final Logger log = LoggerFactory.getLogger(GameManageServiceImpl.class);
	
	@Autowired
	GameAreaRepository gameAreaRepository;
	
	@Autowired
	GameServerRepository gameServerRepository;
	
	@Autowired
	GameGroupRepository gameGroupRepository;
	
	/**
	 * 根据游戏服务器id获取分区id
	 */
	@Override
	public GameAreaDTO findByServerId(Long serverId) 
			throws MissingParamsException,GameAreaNotFoundException,GameServerNotFoundException{
		String paramMsg = "query - serverId:"+serverId;
		log.info(paramMsg);
		if(serverId == 0){
			log.error("params:"+paramMsg+",result:serverId is null");
			throw new MissingParamsException("serverId is null");
		}
		
		//获取服务器信息
		List<GameServerPo> gameServerPoList = gameServerRepository.findById(serverId);
		if( null==gameServerPoList){
			throw new GameServerNotFoundException(new String[]{"GameServerPo not found"});
		}
		
		GameAreaDTO gameAreaDTO = null;
		if( null!=gameServerPoList && gameServerPoList.size()>0){
			GameServerPo gameServerPo = gameServerPoList.get(0);
			//获取对应的分区信息
			List<GameAreaPo> gameAreaPoList = gameAreaRepository.findById(gameServerPo.getGameAreaId());
			
			if( null!=gameAreaPoList && gameAreaPoList.size()>0){
				gameAreaDTO = new GameAreaDTO();
				DozerBeanMapper beanMapper = new DozerBeanMapper();
				beanMapper.map(gameAreaPoList.get(0), gameAreaDTO);
			}
			if(null == gameAreaDTO) {
				throw new GameAreaNotFoundException(new String[]{"gameAreaDTO not found"});
			}
		}
		
		return gameAreaDTO;
	}

	/**
	 * 获取游戏网络类型、分区信息
	 */
	@Override
	public List<GameGroupDTO> findByGameIdAndStateAndType(Long gameId, String state, String type)
			throws MissingParamsException, GameGroupNotFoundException, SystemException{

		String paramMsg = "query - gameId:"+gameId+",state:"+state+",type:"+type;
		log.info(paramMsg);
		
		if(gameId == 0 || StringUtils.isBlank(state) || StringUtils.isBlank(type)){
			log.error("params:"+paramMsg+",result:gameId or state or type is null");
			throw new MissingParamsException("gameId or state or type is null");
		}
		
		//获取对应游戏的网络类型和分区对应关系
		List<GameGroupPo> gameGroupPoList = gameGroupRepository.findByGameIdAndStateAndType(gameId,state,type);
		
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		List<GameGroupDTO> gameGroupDTOList = new ArrayList<GameGroupDTO>(gameGroupPoList.size());
		
		for(GameGroupPo _gameGroupPo:gameGroupPoList){
			GameGroupDTO dto = new GameGroupDTO();
			Set<GameAreaPo> gameAreaPoSet = _gameGroupPo.getGameAreaSet();
			boolean flag = false;
			for(GameAreaPo gameAreaPo:gameAreaPoSet){
				if(StringUtils.equals(gameAreaPo.getState(), "1") || StringUtils.equals(gameAreaPo.getState(), "3"));
				flag = true;
			}
			if(flag){//如果满足gamearea.state=1 or gamearea.state=3,追加
				beanMapper.map(_gameGroupPo, dto);
				gameGroupDTOList.add(dto);
			}else{
				continue;
			}
		}
		
		if(gameGroupDTOList.size()==0) {
			throw new GameGroupNotFoundException(new String[]{"GameGroup not found"});
		}
		
		return gameGroupDTOList;
	}
}
