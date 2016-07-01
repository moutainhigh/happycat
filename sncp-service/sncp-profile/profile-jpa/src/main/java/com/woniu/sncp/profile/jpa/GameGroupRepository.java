package com.woniu.sncp.profile.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.profile.po.GameGroupPo;

public interface GameGroupRepository extends CrudRepository<GameGroupPo, Long> {

//	@Query("FROM GameGroupPo c where c.gameId=:gameId and c.state=:state and c.type=:type")
	public List<GameGroupPo> findByGameIdAndStateAndType(Long gameId,String state,String type);
}
