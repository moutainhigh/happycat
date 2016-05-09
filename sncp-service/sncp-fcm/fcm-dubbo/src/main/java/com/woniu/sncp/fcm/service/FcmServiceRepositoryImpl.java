package com.woniu.sncp.fcm.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;
import com.woniu.sncp.fcm.mongo.repository.PassportFcmTotalTimeRepository;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.passport.service.PassportService;

public class FcmServiceRepositoryImpl implements FcmService{

	private final static long RESET_TIME_SECONDS = 18000L;
	private final static long LEAVE_TIME_SECONDS = 600L;
	
	//防沉迷 状态 验证中 - 2
	private static final String FCM_STATUS_AUTH_ING = "2";

	// 防沉迷 状态  验证通过 - 3
	private static final String FCM_STATUS_PASSED = "3";
	
	protected static final Logger log = LoggerFactory.getLogger(FcmServiceRepositoryImpl.class);
	
	@Autowired PassportFcmTotalTimeRepository repository;
	@Autowired FcmGameProfileRepositoryImpl gameProfileRepository;
	@Autowired PassportService passportService;

	@Override
	public boolean isFcm(Long accountId,Long aoId,Long gameId) throws PassportNotFoundException {
		
		//检查游戏是否需要防沉迷
		FcmGameProfileTo gameProfile = gameProfileRepository.query(aoId, gameId);
		if(gameProfile == null) return false;
		
		try {
			PassportDto passport = passportService.findPassportByAid(accountId);
			
			Date fcmDay = DateUtils.addYears(new Date(), -18);
			Date birthDay = passport.getIdentityBirthday();
			
			//18周岁 s_ispass =‘3’
			if(FCM_STATUS_PASSED.equals(passport.getIdentityAuthState())
					&& birthDay.before(fcmDay)){
				return false;
			}
			
			//18周岁 s_ispass =2 身份证和名字不为空
			if(FCM_STATUS_AUTH_ING.equals(passport.getIdentityAuthState())
					&& birthDay.before(fcmDay)
					&& StringUtils.isNotBlank(passport.getIdentity())
					&& StringUtils.isNotBlank(passport.getName())
					){
				return false;
			}
			
			//3天内注册 且身份证和名字为空
			Date registerDay = passport.getCreateDate();
			Date registerNotLimitDay = DateUtils.addYears(new Date(), -3);
			if(registerDay.after(registerNotLimitDay)
					&& StringUtils.isBlank(passport.getIdentity())
					&& StringUtils.isBlank(passport.getName())
					){
				return false;
			}
		} catch (SystemException e) {
			log.error(e.getMessage(),e);
			//TODO: 告警处理
			return false;//系统异常降级处理 
		}
		
		return true;
	}

	@Override
	public Long fcmOnlineTime(String identity, Long gameId) {
		Long onlineTimeSeconds = 0L;

		try{
			PassportFcmTotalTimeTo fcmTotalTimeTo = queryUserFcmTotalTime(identity, gameId);
			log.info("jpa query fcmTotalTime:"+fcmTotalTimeTo);
			
			if(fcmTotalTimeTo == null){
				fcmTotalTimeTo = new PassportFcmTotalTimeTo();
				fcmTotalTimeTo.setGameId(gameId);
				fcmTotalTimeTo.setIdentity(identity);
				fcmTotalTimeTo.setLastChange(new Date());
				fcmTotalTimeTo.setTime(0L);
				fcmTotalTimeTo.setLeaveTime(0L);
				repository.save(new DozerBeanMapper().map(fcmTotalTimeTo, PassportFcmTotalTimePo.class));
				return onlineTimeSeconds;
			}
			
			Date now = new Date();
			Long intervalTimeSeconds = (now.getTime()-fcmTotalTimeTo.getLastChange().getTime())/1000;
			log.info("jpa intervalTimeSeconds:"+intervalTimeSeconds);
			
			fcmTotalTimeTo.setLastChange(now);
			//离线超过5小时 重新计数在线时间
			if(intervalTimeSeconds > RESET_TIME_SECONDS){ //离线 大于5小时
				fcmTotalTimeTo.setLeaveTime(0L);
				fcmTotalTimeTo.setTime(0L);
	
				repository.save(new DozerBeanMapper().map(fcmTotalTimeTo, PassportFcmTotalTimePo.class));
				return onlineTimeSeconds;
			}
			
			
			if(intervalTimeSeconds  > LEAVE_TIME_SECONDS){ // 大于10分钟
				Long reNewLeaveTime = 0L;
				reNewLeaveTime = intervalTimeSeconds
									+ fcmTotalTimeTo.getLeaveTime();
				if(reNewLeaveTime > RESET_TIME_SECONDS){ //累计离线 大于5小时
					fcmTotalTimeTo.setTime(intervalTimeSeconds);
					fcmTotalTimeTo.setLeaveTime(0L);
				} else {
					fcmTotalTimeTo.setLeaveTime(reNewLeaveTime);
				}
			} else { // 小于等于10分钟
				Long reNewOnlineTime = 0L;
				reNewOnlineTime = intervalTimeSeconds + fcmTotalTimeTo.getTime();
				fcmTotalTimeTo.setTime(reNewOnlineTime);
			}
			
			log.info("jpa update before:"+fcmTotalTimeTo);
			repository.save(new DozerBeanMapper().map(fcmTotalTimeTo, PassportFcmTotalTimePo.class));
			
			onlineTimeSeconds = fcmTotalTimeTo.getTime();
		
		} catch (SystemException e){//系统异常降级处理 
			log.error(e.getMessage(),e);
			//TODO: 告警处理
		}
		
		return onlineTimeSeconds;
	}

	@Override
	public PassportFcmTotalTimeTo queryUserFcmTotalTime(String identity, Long gameId) {
		PassportFcmTotalTimeTo ppFcmTotalTimeTo = null;
		try{
			
			List<PassportFcmTotalTimePo> find = repository.findByIdentityAndGameId(identity,gameId);
			
			if(find != null && !find.isEmpty()){
				DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
				ppFcmTotalTimeTo =  dozerBeanMapper.map(find.get(0), PassportFcmTotalTimeTo.class);
			}
		} catch (MongoException e){
			return null;
		}
		
		log.info("jpa query fcm total time:"+ppFcmTotalTimeTo);
		return ppFcmTotalTimeTo;
	}

	public String queryIdentity(Long accountId) throws PassportNotFoundException{
		try{
			PassportDto passport = passportService.findPassportByAid(accountId);
			Date fcmDay = DateUtils.addYears(new Date(), -18);
			Date birthDay = passport.getIdentityBirthday();
			
			if(birthDay.before(fcmDay)
					|| StringUtils.isBlank(passport.getIdentity())){
				return passport.getId() + "";
			}
			
			return passport.getIdentity();
		} catch (SystemException e) {
			log.error(e.getMessage(),e);
			//TODO: 告警处理
		}
		
		return accountId+"";
	}
}
