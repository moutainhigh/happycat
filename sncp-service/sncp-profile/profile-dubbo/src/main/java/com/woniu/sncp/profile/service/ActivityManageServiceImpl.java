package com.woniu.sncp.profile.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.profile.dao.ActivityManageDao;
import com.woniu.sncp.profile.dto.AllActivityDTO;
import com.woniu.sncp.profile.dto.PassportPresentsPloyDTO;
import com.woniu.sncp.profile.dto.PassportPresentsPloyDetailDTO;
import com.woniu.sncp.profile.po.PassportPresentsPloyDetailPo;
import com.woniu.sncp.profile.po.PassportPresentsPloyPo;
import com.woniu.sncp.profile.service.ploy.PresentsPloyContext;

/**
 * <p>descrption: 活动信息管理实现</p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class ActivityManageServiceImpl implements ActivityManageService {

	protected static final Logger log = LoggerFactory.getLogger(ActivityManageServiceImpl.class);
			
	@Autowired
	ActivityManageDao activityManageDao;
	
	@Autowired
	PresentsPloyContext presentsPloyContext;
	
	/**
	 * 查询所有活动
	 */
	@Override
	public AllActivityDTO findAllPloysByState(Long gameId,String state)
			throws MissingParamsException{
		String paramMsg = "query - state:"+state;
		log.info(paramMsg);
		if(ObjectUtils.isEmpty(state)){
			log.error("params:"+paramMsg+",result:state is null");
			throw new MissingParamsException("state is null");
		}
		
		//所有活动类型
		Field[] declaredFields = presentsPloyContext.getClass().getDeclaredFields();
		List<String> ployTypes = new ArrayList<String>();
		for (int i = 0; i < declaredFields.length; i++) {
			String name = declaredFields[i].getName();
			if(name.indexOf('_') > -1){
				ployTypes.add(name.split("_")[1]);
			}
		}
		
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		//1.查询所有活动
		List<PassportPresentsPloyPo> passportPresentsPloyPoList = activityManageDao.findAllByStateAndPloyTypes(state, ployTypes);
		
		List<PassportPresentsPloyDTO> passportPresentsPloyDTOList = new ArrayList<PassportPresentsPloyDTO>(passportPresentsPloyPoList.size());
		
		//2.过滤游戏，充值平台，运营商
		String sGameId = ","+gameId+",";
		List<Integer> _ployIds = new ArrayList<Integer>();
		for(PassportPresentsPloyPo ployPo:passportPresentsPloyPoList){
			PassportPresentsPloyDTO dto = new PassportPresentsPloyDTO();
			
			String limitGames = ployPo.getLimitGame();//限制游戏
			String limitOperators = ployPo.getLimitIssuer();//限制运营商
			String limitAgents = ployPo.getLimitPaymentPlatform();//限制支付平台ID
			
			String sLimitGames = ","+limitGames+",";
			String sLimitOperators = ","+limitOperators+",";
			String sLimitAgents = ","+limitAgents+",";
			
			if((StringUtils.isEmpty(limitGames) || sLimitGames.indexOf(sGameId) > -1)
					&& (StringUtils.isEmpty(limitOperators) || sLimitOperators.indexOf(",7,") > -1)
					&& (StringUtils.isEmpty(limitAgents) )){
				beanMapper.map(ployPo, dto);
				passportPresentsPloyDTOList.add(dto);
				
				_ployIds.add(Integer.valueOf(String.valueOf(ployPo.getId())));
			}
			
		}
		//3.所有活动id的活动详情
		List<Integer> ployIds = new ArrayList<Integer>();
		for (PassportPresentsPloyPo po : passportPresentsPloyPoList) {
			ployIds.add(Integer.valueOf(String.valueOf(po.getId())));
		}
		List<PassportPresentsPloyDetailPo> passportPresentsPloyDetailPoList= activityManageDao.findAllByStateAndPloyIds(ployIds);
		
		List<PassportPresentsPloyDetailDTO> passportPresentsPloyDetailDTOList = new ArrayList<PassportPresentsPloyDetailDTO>(passportPresentsPloyDetailPoList.size());
		for(PassportPresentsPloyDetailPo detailPo:passportPresentsPloyDetailPoList){
			PassportPresentsPloyDetailDTO detailDTO = new PassportPresentsPloyDetailDTO();
			
			if(_ployIds.contains(Integer.valueOf(String.valueOf(detailPo.getId())))){
				beanMapper.map(detailPo,detailDTO);
				passportPresentsPloyDetailDTOList.add(detailDTO);
			}
		}
		
		//所有活动对象拼装返回
		AllActivityDTO result = new AllActivityDTO();
		result.setPloys(passportPresentsPloyDTOList);
		result.setDetails(passportPresentsPloyDetailDTOList);
		
		return result;
	}

}
