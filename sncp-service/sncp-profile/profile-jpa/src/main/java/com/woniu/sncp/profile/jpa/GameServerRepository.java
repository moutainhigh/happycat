package com.woniu.sncp.profile.jpa;


import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.woniu.sncp.profile.po.GameServerPo;

public interface GameServerRepository extends CrudRepository<GameServerPo, Long> {

	public List<GameServerPo> findById(Long serverId);
}
