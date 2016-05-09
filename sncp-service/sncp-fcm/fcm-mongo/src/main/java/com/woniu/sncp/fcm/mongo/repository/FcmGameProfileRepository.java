package com.woniu.sncp.fcm.mongo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.woniu.sncp.fcm.mongo.FcmGameProfilePo;

public interface FcmGameProfileRepository extends CrudRepository<FcmGameProfilePo, Long>{
	List<FcmGameProfilePo> queryByAoId(Long aoId);
	FcmGameProfilePo queryByAoIdAndGameId(Long aoId,Long gameId);
	Long deleteByAoIdAndGameId(Long aoId,Long gameId);
}
