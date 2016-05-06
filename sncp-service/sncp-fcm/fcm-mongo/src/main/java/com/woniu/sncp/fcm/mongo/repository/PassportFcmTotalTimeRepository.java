package com.woniu.sncp.fcm.mongo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.woniu.sncp.fcm.mongo.PassportFcmTotalTimePo;

public interface PassportFcmTotalTimeRepository extends CrudRepository<PassportFcmTotalTimePo, String>{
	List<PassportFcmTotalTimePo> findByIdentityAndGameId(String identity,Long gameId);
}
