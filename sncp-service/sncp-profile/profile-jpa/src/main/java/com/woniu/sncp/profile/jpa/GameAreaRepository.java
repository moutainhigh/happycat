package com.woniu.sncp.profile.jpa;


import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.woniu.sncp.profile.po.GameAreaPo;

public interface GameAreaRepository extends CrudRepository<GameAreaPo, Long> {

	public List<GameAreaPo> findById(Long id);
}
