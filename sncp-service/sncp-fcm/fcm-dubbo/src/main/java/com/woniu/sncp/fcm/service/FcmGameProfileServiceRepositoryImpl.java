package com.woniu.sncp.fcm.service;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.woniu.sncp.fcm.dto.FcmGameProfileTo;
import com.woniu.sncp.fcm.mongo.FcmGameProfilePo;
import com.woniu.sncp.fcm.mongo.repository.FcmGameProfileRepository;

public class FcmGameProfileServiceRepositoryImpl implements FcmGameProfileService{

	@Autowired FcmGameProfileRepository repository;
	
	@Override
	public void save(FcmGameProfileTo fcmGameProfileTo) {
		repository.save(new DozerBeanMapper().map(fcmGameProfileTo, FcmGameProfilePo.class));
	}

	@Override
	public Long delete(Long aoId, Long gameId) {
		return repository.deleteByAoIdAndGameId(aoId, gameId);
	}

	@Override
	public List<FcmGameProfileTo> query(Long aoId) {
		
		 List<FcmGameProfilePo> fcmGameProfilePoList = repository.queryByAoId(aoId);
		 
		 List<FcmGameProfileTo> fcmGameProfileToList = new ArrayList<FcmGameProfileTo>();
		 if(fcmGameProfilePoList != null && !fcmGameProfilePoList.isEmpty()){
			 for (FcmGameProfilePo po : fcmGameProfilePoList) {
				 fcmGameProfileToList.add(new DozerBeanMapper().map(po, FcmGameProfileTo.class));
			}
		 }
		 
		 return fcmGameProfileToList;
	}

	@Override
	public FcmGameProfileTo query(Long aoId, Long gameId) {
		
		 FcmGameProfilePo fcmGameProfilePo = repository.queryByAoIdAndGameId(aoId, gameId);
		 
		 FcmGameProfileTo fcmGameProfileTo = null;
		 if(fcmGameProfilePo != null){
			 fcmGameProfileTo = new DozerBeanMapper().map(fcmGameProfilePo, FcmGameProfileTo.class);
		 }
		 
		 return fcmGameProfileTo;
	}

}
