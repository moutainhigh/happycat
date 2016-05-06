package com.woniu.sncp.fcm.services;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.util.Date;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;

public class FcmServiceImpl implements FcmService{

	private final static long RESET_TIME_SECONDS = 18000L;
	private final static long LEAVE_TIME_SECONDS = 600L;
	
	protected static final Logger log = LoggerFactory.getLogger(FcmServiceImpl.class);
		
	@Autowired
	private MongoTemplate mongoOps;
	
	@Override
	public boolean isFcm(Long accountId, Long gameId) {
		
		return false;
	}

	@Override
	public Long queryFcmTime(Long accountId,Long gameId) {
		Long onlineTimeSeconds = 0L;
		
		PassportFcmTotalTimeTo fcmTotalTimeTo = queryUserFcmTotalTime(accountId, gameId);
		log.info("query fcmTotalTime:"+fcmTotalTimeTo);
		
		if(fcmTotalTimeTo == null){
			fcmTotalTimeTo = new PassportFcmTotalTimeTo();
			fcmTotalTimeTo.setGameId(gameId);
			fcmTotalTimeTo.setIdentity(accountId+"");
			fcmTotalTimeTo.setLastChange(new Date());
			fcmTotalTimeTo.setTime(0L);
			fcmTotalTimeTo.setLeaveTime(0L);
			createUserFcmTotalTime(fcmTotalTimeTo);
			return onlineTimeSeconds;
		}
		
		
		Date now = new Date();
		Long intervalTimeSeconds = (now.getTime()-fcmTotalTimeTo.getLastChange().getTime())/1000;
		log.info("intervalTimeSeconds:"+intervalTimeSeconds);
		
		fcmTotalTimeTo.setLastChange(now);
		//离线超过5小时 重新计数在线时间
		if(intervalTimeSeconds > RESET_TIME_SECONDS){ //离线 大于5小时
			fcmTotalTimeTo.setLeaveTime(0L);
			fcmTotalTimeTo.setTime(0L);
			
			updateUserFcmTotalTime(fcmTotalTimeTo);
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
		
		log.info("update before:"+fcmTotalTimeTo);
		updateUserFcmTotalTime(fcmTotalTimeTo);
		
		return fcmTotalTimeTo.getTime();
	}
	
	public void createUserFcmTotalTime(PassportFcmTotalTimeTo fcmTotalTime){
		mongoOps.save(new DozerBeanMapper().map(fcmTotalTime, PassportFcmTotalTimePo.class));
	}
	
	public boolean updateUserFcmTotalTime(PassportFcmTotalTimeTo fcmTotalTime){
		WriteResult upsert = mongoOps.upsert(query(where("identity").is(fcmTotalTime.getIdentity()+"")
							 .andOperator(where("gameId").is(fcmTotalTime.getGameId())))
				, update("leaveTime",fcmTotalTime.getLeaveTime()).set("time", fcmTotalTime.getTime()).set("lastChange", fcmTotalTime.getLastChange())
				, PassportFcmTotalTimePo.class);
		
		return upsert.isUpdateOfExisting();
	}

	@Override
	public PassportFcmTotalTimeTo queryUserFcmTotalTime(Long accountId,Long gameId) {
		PassportFcmTotalTimeTo ppFcmTotalTimeTo = null;
		try{
			List<PassportFcmTotalTimePo> find = mongoOps.find(query(where("identity").is(accountId+"")), PassportFcmTotalTimePo.class);
			
			if(find != null && !find.isEmpty()){
				DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
				ppFcmTotalTimeTo =  dozerBeanMapper.map(find.get(0), PassportFcmTotalTimeTo.class);
			}
		} catch (MongoException e){
			return null;
		}
		
		return ppFcmTotalTimeTo;
	}

}
