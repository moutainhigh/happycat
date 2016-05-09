package com.woniu.sncp.fcm.service;

import java.util.Date;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoException;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;
import com.woniu.sncp.fcm.mongo.repository.PassportFcmTotalTimeRepository;

public class FcmServiceRepositoryImpl implements FcmService{

	private final static long RESET_TIME_SECONDS = 18000L;
	private final static long LEAVE_TIME_SECONDS = 600L;
	
	protected static final Logger log = LoggerFactory.getLogger(FcmServiceImpl.class);
	
	@Autowired PassportFcmTotalTimeRepository repository;

	@Override
	public boolean isFcm(Long accountId, Long gameId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Long fcmOnlineTime(Long accountId, Long gameId) {
		Long onlineTimeSeconds = 0L;
		
		PassportFcmTotalTimeTo fcmTotalTimeTo = queryUserFcmTotalTime(accountId, gameId);
		log.info("jpa query fcmTotalTime:"+fcmTotalTimeTo);
		
		if(fcmTotalTimeTo == null){
			fcmTotalTimeTo = new PassportFcmTotalTimeTo();
			fcmTotalTimeTo.setGameId(gameId);
			fcmTotalTimeTo.setIdentity(accountId+"");
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
		
		return fcmTotalTimeTo.getTime();
	}

	@Override
	public PassportFcmTotalTimeTo queryUserFcmTotalTime(Long accountId, Long gameId) {
		PassportFcmTotalTimeTo ppFcmTotalTimeTo = null;
		try{
			
			List<PassportFcmTotalTimePo> find = repository.findByIdentityAndGameId(accountId+"",gameId);
			
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

}
