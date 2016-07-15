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

	protected static final Logger logger = LoggerFactory.getLogger(ActivityManageServiceImpl.class);
			
	@Autowired
	ActivityManageDao activityManageDao;
	
	@Autowired
	PresentsPloyContext presentsPloyContext;
	
//	@Autowired 
//	PassportService passportService;
	
	/**
	 * 查询所有活动
	 */
	@Override
	public List<PassportPresentsPloyDTO> findAllPloysByState(Long gameId,String state)
			throws MissingParamsException{
		String paramMsg = "query - state:"+state;
		logger.info(paramMsg);
		if(ObjectUtils.isEmpty(state)){
			logger.error("params:"+paramMsg+",result:state is null");
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
		//details整理到ploys里面
		for(PassportPresentsPloyDTO ploy:passportPresentsPloyDTOList){
			List<PassportPresentsPloyDetailDTO> _detailDTO = new ArrayList<PassportPresentsPloyDetailDTO>();
			for(PassportPresentsPloyDetailDTO detail:passportPresentsPloyDetailDTOList){
				if(ploy.getId().equals(detail.getPloyId())){
					_detailDTO.add(detail);
				}
			}
			ploy.setDetails(_detailDTO);//设置活动对应的详情
		}
		
		return passportPresentsPloyDTOList;
	}

	
//	@Override
//	public ActivityDTO findOfficalPloys(Boolean isEaiQuery,String impLogId,Long gameId, Long platformId, Long areaId, Long cardTypeId,
//			String imprestDestination, Integer count, String account, String decodeType, String valueAmount,
//			String issuerId) throws MissingParamsException{
//		// 
//		return null;
//	}
//
//	
//	@Override
//	public ActivityDTO findSnailCardPloys(Boolean isEaiQuery,String impLogId,Long gameId, Long platformId, Long areaId, Long cardTypeId,
//			String imprestDestination, Integer count, String account, String decodeType, String valueAmount,
//			String issuerId, String cardNo, String cardPwd) throws MissingParamsException,ValidationException,PassportNotFoundException,PassportHasFrozenException,PassportHasLockedException,Exception {
//
//		if (logger.isDebugEnabled()) {
//			logger.debug("imprestDestination: " + imprestDestination);
//		}
//		
//		try {
//			if (StringUtils.isBlank(cardNo)
//					|| StringUtils.isBlank(cardPwd)
//					|| StringUtils.isBlank(imprestDestination)
//					|| StringUtils.isBlank(account)) {
//				throw new MissingParamsException("cardNo or cardPwd or imprestDestination or account is null");
//			}
//			//查询账号
//			PassportDto passport = passportService.findPassportByAccountOrAliase(account);
//			
//			//判断卡号
//			if (StringUtils.isBlank(cardNo) || cardNo.length() < 10) {
//				logger.error("卡号不正确，卡号：" + cardNo);
//				throw new ValidationException("卡号或密码不正确!");
//			}
//			
//			//查询卡信息
//			//SnailCard snailCard = imprestService.querySnailCard(cardNo);
//			
//			if (!isEaiQuery) { // 不是eai查询调用，则须校验密码
////				cardPwd = DesUtil.getCString(snailCard.getPassword(), cardPwd);
////				if (StringUtils.isBlank(cardPwd)
////						|| !StringUtils
////								.equals(cardPwd, snailCard.getPassword())) {
////					logger.error("卡号不正确，卡号：" + cardNo);
////					throw new ValidationException("卡号或密码不正确!");
////				}
//
//				// eai查询就不需要验证
////				if (SnailCard.STATE_NOT_ACTIVE.equals(snailCard.getState()))
////					throw new ValidationException("此卡未激活");
////				if (SnailCard.STATE_USED.equals(snailCard.getState()))
////					throw new ValidationException("此卡已被使用");
////				if (SnailCard.STATE_FREEZE.equals(snailCard.getState()))
////					throw new ValidationException("此卡已被冻结");
////				if (SnailCard.STATE_NOT_VALID.equals(snailCard.getState()))
////					throw new ValidationException("此卡已被作废");
//			}
//			
//			// 一卡通的支付平台属于官方直充 - 100
//			List<Object[]> propList = null;
////			if (!isEaiQuery) {
////				propList = imprestPloyService.imprestPloy(
////						snailCard.getCardTypeId(), 1, 100L, gameId,
////						("0".equals(imprestDestination) ? 0L : areaId), areaId,
////						account, true, 0L, isEaiQuery, snailCard,new SingletonMap());
////			} else {
////				propList = imprestPloyService.imprestPloy(
////						snailCard.getCardTypeId(), 1, 100L, gameId,
////						("0".equals(imprestDestination) ? 0L : areaId), areaId,
////						account, true, Long.parseLong(impLogId), isEaiQuery,
////						snailCard,new SingletonMap());
////			}
//			
//			
//			
//		} catch (PassportNotFoundException e) {
//			throw new PassportNotFoundException("用户账号不存在");
//		} catch (PassportHasFrozenException e) {
//			throw new PassportHasFrozenException("帐号被冻结");
//		} catch (PassportHasLockedException e) {
//			throw new PassportHasLockedException("帐号被锁定");
//		}catch (Exception e) {
//			throw new Exception("操作异常");
//		}
//		
//		
//		
//		return null;
//	}

}
