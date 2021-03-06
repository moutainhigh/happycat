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
import com.woniu.sncp.alarm.dto.AlarmMessageTo;
import com.woniu.sncp.alarm.service.AlarmMessageService;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.config.AlarmPropertiesConfig;
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
	@Autowired FcmGameProfileServiceRepositoryImpl gameProfileService;
	@Autowired PassportService passportService;
	@Autowired AlarmMessageService alarmMessageService;
	@Autowired AlarmPropertiesConfig alarmConfig;

	@Override
	public boolean isFcm(Long accountId,Long aoId,Long gameId,boolean validateThreeCondition) throws PassportNotFoundException {
		String paramsMsg = "is fcm - accountId:"+accountId+",aoId:"+aoId+",gameId:"+gameId+",validateThreeCondition:"+validateThreeCondition;
		log.info(paramsMsg);
		
		if( accountId == null
				|| aoId == null
				|| gameId == null){
			log.error("accountId or aoId or gameId is null");
			throw new MissingParamsException("accountId or aoId or gameId is null");
		}
		
		try {
			//检查游戏是否需要防沉迷
			FcmGameProfileTo gameProfile = gameProfileService.query(aoId, -gameId);//配置表游戏为负数时防沉迷
			if(gameProfile == null) {
				log.info(paramsMsg +" - gameProfile is null,fcm return false");
				return false;
			}
			
			PassportDto passport = passportService.findPassportByAid(accountId);
			log.info(paramsMsg +" - "+passport);
			Date fcmDay = DateUtils.addYears(new Date(), -18);
			Date birthDay = passport.getIdentityBirthday();
			
			//18周岁 s_ispass =‘3’
			if(FCM_STATUS_PASSED.equals(passport.getIdentityAuthState())
					&& birthDay != null
					&& birthDay.before(fcmDay)){
				log.info(paramsMsg +" - ispass is 3,fcm return false");
				return false;
			}
			
			//18周岁 s_ispass =2 身份证和名字不为空
			if(validateThreeCondition
					&& FCM_STATUS_AUTH_ING.equals(passport.getIdentityAuthState())
					&& StringUtils.isNotBlank(passport.getIdentity())
					&& StringUtils.isNotBlank(passport.getName())
					&& birthDay != null
					&& birthDay.before(fcmDay)
					){
				log.info(paramsMsg + " - ispass is 2,fcm return false");
				return false;
			}
			
			//3天内注册 且身份证和名字为空
			Date registerDay = passport.getCreateDate();
			Date registerNotLimitDay = DateUtils.addDays(new Date(), -3);
			if(validateThreeCondition
					&& registerDay.after(registerNotLimitDay)
					&& StringUtils.isBlank(passport.getIdentity())
					&& StringUtils.isBlank(passport.getName())
					){
				log.info(paramsMsg +" - 3 day ,fcm return false");
				return false;
			}
			
		} catch (Exception e) {
			log.error(paramsMsg +" - exception ,fcm return false",e);
			alarmMessageService.sendMessage(new AlarmMessageTo(alarmConfig.getSrc(), "防沉迷判断异常[已降级处理]，"+e.getMessage()));
			return false;//系统异常降级处理 
		}
		
		log.info(paramsMsg +" - fcm return true");
		return true;
	}

	@Override
	public Long fcmOnlineTime(String identity, Long gameId) {
		log.info("fcmOnlineTime - identity:"+identity+",gameId:"+gameId);

		if( StringUtils.isBlank(identity)
				|| gameId == null){
			throw new MissingParamsException("identity or gameId is null");
		}
		
		Long onlineTimeSeconds = 0L;

		try{
			PassportFcmTotalTimeTo fcmTotalTimeTo = queryUserFcmTotalTime(identity, gameId);
			
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
			log.info("fcmOnlineTime - identity:"+identity+",gameId:"+gameId+",intervalTimeSeconds:"+intervalTimeSeconds);
			
			fcmTotalTimeTo.setLastChange(now);
			//离线超过5小时 重新计数在线时间
			if(intervalTimeSeconds > RESET_TIME_SECONDS){ //离线 大于5小时
				fcmTotalTimeTo.setLeaveTime(0L);
				fcmTotalTimeTo.setTime(0L);
	
				repository.save(new DozerBeanMapper().map(fcmTotalTimeTo, PassportFcmTotalTimePo.class));
				
				log.info("fcmOnlineTime > 5 hours - _id:"+fcmTotalTimeTo.getId()+"identity:"+identity+",gameId:"+gameId+",onlineTimeSeconds:"+onlineTimeSeconds);
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
				
				log.info("fcmOnlineTime > 10 mins - identity:"+identity+",gameId:"+gameId+",fcmTotalTimeTo:"+fcmTotalTimeTo);
			} else { // 小于等于10分钟
				Long reNewOnlineTime = 0L;
				reNewOnlineTime = intervalTimeSeconds + fcmTotalTimeTo.getTime();
				fcmTotalTimeTo.setTime(reNewOnlineTime);
				
				log.info("fcmOnlineTime <= 10 mins - identity:"+identity+",gameId:"+gameId+",fcmTotalTimeTo:"+fcmTotalTimeTo);
			}
			
			repository.save(new DozerBeanMapper().map(fcmTotalTimeTo, PassportFcmTotalTimePo.class));
			
			onlineTimeSeconds = fcmTotalTimeTo.getTime();
		
		} catch (Exception e){//系统异常降级处理 
			log.error(e.getMessage(),e);
			alarmMessageService.sendMessage(new AlarmMessageTo(alarmConfig.getSrc(), "防沉迷计时异常[已降级处理]，"+e.getMessage()));
		}
		
		log.info("fcmOnlineTime finished - identity:"+identity+",gameId:"+gameId+",onlineTimeSeconds:"+onlineTimeSeconds);
		return onlineTimeSeconds;
	}

	@Override
	public PassportFcmTotalTimeTo queryUserFcmTotalTime(String identity, Long gameId) {
		if( StringUtils.isBlank(identity)
				|| gameId == null){
			throw new MissingParamsException("identity or gameId is null");
		}
		
		PassportFcmTotalTimeTo ppFcmTotalTimeTo = null;
		try{
			
			List<PassportFcmTotalTimePo> find = repository.findByIdentityAndGameId(identity,gameId);
			
			if(find != null && !find.isEmpty()){
				DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
				ppFcmTotalTimeTo =  dozerBeanMapper.map(find.get(0), PassportFcmTotalTimeTo.class);
			}
		} catch (MongoException e){
			alarmMessageService.sendMessage(new AlarmMessageTo(alarmConfig.getSrc(), "Mongo库异常，"+e.getMessage()));
			return null;
		}
		
		log.info("jpa query fcm total time - identity:"+identity+",gameId:"+gameId+",result:"+ppFcmTotalTimeTo);
		return ppFcmTotalTimeTo;
	}

	public String queryIdentity(Long accountId) throws PassportNotFoundException{
		if( accountId == null){
			throw new MissingParamsException("accountId is null");
		}
		
		try{
			PassportDto passport = passportService.findPassportByAid(accountId);
			log.info("query identity - accountId:"+accountId+",passport:"+passport);
			Date fcmDay = DateUtils.addYears(new Date(), -18);
			Date birthDay = passport.getIdentityBirthday();
			
			if((birthDay != null && birthDay.before(fcmDay))
					|| StringUtils.isBlank(passport.getIdentity())){
				return passport.getId() + "";
			}
			
			log.info("query identity - accountId:"+accountId+",result:"+passport.getIdentity());
			return passport.getIdentity();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			alarmMessageService.sendMessage(new AlarmMessageTo(alarmConfig.getSrc(), "防沉迷唯一标识查询异常[已降级处理]，"+e.getMessage()));
		}
		
		log.info("query identity - accountId:"+accountId+",result:"+accountId);
		return accountId+"";
	}

	@Override
	public void save(PassportFcmTotalTimeTo passportFcmTotalTimeTo) throws MissingParamsException, SystemException {
		repository.save(new DozerBeanMapper().map(passportFcmTotalTimeTo, PassportFcmTotalTimePo.class));
	}
}
