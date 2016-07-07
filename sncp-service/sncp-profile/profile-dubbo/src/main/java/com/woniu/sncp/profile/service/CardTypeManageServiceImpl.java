package com.woniu.sncp.profile.service;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.profile.dao.CardManageDao;
import com.woniu.sncp.profile.dto.CardDetailDTO;
import com.woniu.sncp.profile.dto.CardValueDTO;
import com.woniu.sncp.profile.po.CardDetailPo;
import com.woniu.sncp.profile.po.CardValuePo;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class CardTypeManageServiceImpl implements CardTypeManageService {

	protected static final Logger log = LoggerFactory.getLogger(CardTypeManageServiceImpl.class);
			
	@Autowired
	CardManageDao cardManageDao;
	
//	@Autowired
//	CardManageRepository cardManageRepository;

	/**
	 * 获取面值大类
	 */
	@Override
	public List<CardValueDTO> findValueByGameIdAndPlatformId(Long gameId, Long platformId) 
			throws MissingParamsException{
		if(gameId == 0){
			String paramMsg = "query - gameId:"+gameId +",platformId:"+platformId;
			log.info(paramMsg);
			log.error("params:"+paramMsg+",result:serverId is null");
			throw new MissingParamsException("serverId is null");
		}
//		List<Object[]> objList = cardManageRepository.findValueByGameIdAndPlatformId(gameId, platformId);
		List<CardValuePo> cardValuePoList = cardManageDao.findValueByGameIdAndPlatformId(gameId, platformId);
		List<CardValueDTO> cardValueDTOList = null;
		if(null != cardValuePoList){
			cardValueDTOList = new ArrayList<CardValueDTO>(cardValuePoList.size());
			for(CardValuePo po:cardValuePoList){
				DozerBeanMapper beanMapper = new DozerBeanMapper();
				CardValueDTO cardValueDTO = new CardValueDTO();
				beanMapper.map(po, cardValueDTO);
				cardValueDTOList.add(cardValueDTO);
			}
		}
		
		return cardValueDTOList;
	}

	/**
	 * 获取详细面值
	 */
	@Override
	public List<CardDetailDTO> findDetailByGameIdAndPlatformId(Long gameId, Long paymentId) {
		String paramMsg = "query - gameId:"+gameId +",paymentId:"+paymentId;
		log.info(paramMsg);
		if(gameId == 0){
			log.error("params:"+paramMsg+",result:serverId is null");
			throw new MissingParamsException("serverId is null");
		}
//		List<Object[]> objList = cardManageRepository.findDetailByGameIdAndPlatformId(gameId, platformId);
		List<CardDetailPo> cardDetailPoList = cardManageDao.findDetailByGameIdAndPlatformId(gameId, paymentId);
		List<CardDetailDTO> cardDetailDTOList = null;
		if(null != cardDetailPoList){
			cardDetailDTOList = new ArrayList<CardDetailDTO>(cardDetailPoList.size());
			for(CardDetailPo po:cardDetailPoList){
				DozerBeanMapper beanMapper = new DozerBeanMapper();
				CardDetailDTO cardDetailDTO = new CardDetailDTO();
				beanMapper.map(po, cardDetailDTO);
				cardDetailDTOList.add(cardDetailDTO);
			}
		}
		return cardDetailDTOList;
	}
	
	
}
