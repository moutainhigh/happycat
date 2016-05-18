package com.woniu.sncp.fcm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;
import com.woniu.sncp.fcm.mongo.FcmGameProfilePo;
import com.woniu.sncp.fcm.mongo.repository.FcmGameProfileRepository;

/**
 * 防沉迷游戏配置 服务类
 * 
 * @author luzz
 *
 */
public class FcmGameProfileServiceRepositoryImpl implements FcmGameProfileService{
	
	protected static final Logger log = LoggerFactory.getLogger(FcmGameProfileServiceRepositoryImpl.class);
	
	@Autowired FcmGameProfileRepository repository;
	
	@Override
	public void save(FcmGameProfileTo fcmGameProfileTo) {
		if(fcmGameProfileTo == null
				|| fcmGameProfileTo.getAoId() == null
				|| fcmGameProfileTo.getGameId() == null){
			throw new MissingParamsException("fcmGameProfileTo or aoId or gameId is null");
		}
		repository.save(new DozerBeanMapper().map(fcmGameProfileTo, FcmGameProfilePo.class));
	}

	@Override
	public Long delete(Long aoId, Long gameId) {
		if( aoId == null
				|| gameId == null){
			throw new MissingParamsException("aoId or gameId is null");
		}
		Long result = repository.deleteByAoIdAndGameId(aoId, gameId);
		log.info("delete game profile - aoId:"+aoId+",gameId:"+gameId+",result:"+result); 
		return result;
	}

	@Override
	public List<FcmGameProfileTo> query(Long aoId) {
		if( aoId == null){
			throw new MissingParamsException("aoId is null");
		}
		List<FcmGameProfilePo> fcmGameProfilePoList = repository.queryByAoId(aoId);
		 
		List<FcmGameProfileTo> fcmGameProfileToList = fcmGameProfilePoList
														.stream()
														.map(o -> new DozerBeanMapper().map(o, FcmGameProfileTo.class))
														.collect(Collectors.toList());
		 
		return fcmGameProfileToList;
	}

	@Override
	public FcmGameProfileTo query(Long aoId, Long gameId) {
		if( aoId == null
				|| gameId == null){
			throw new MissingParamsException("aoId or gameId is null");
		}
		FcmGameProfilePo fcmGameProfilePo = repository.queryByAoIdAndGameId(aoId, gameId);
		 
		FcmGameProfileTo fcmGameProfileTo = null;
		if(fcmGameProfilePo != null){
			fcmGameProfileTo = new DozerBeanMapper().map(fcmGameProfilePo, FcmGameProfileTo.class);
		}
		
		return fcmGameProfileTo;
	}

}
