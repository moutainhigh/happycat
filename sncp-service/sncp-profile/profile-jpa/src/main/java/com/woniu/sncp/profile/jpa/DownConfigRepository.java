package com.woniu.sncp.profile.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.woniu.sncp.profile.po.DownConfigPo;

public interface DownConfigRepository extends CrudRepository<DownConfigPo, Long> {//
	Page<DownConfigPo> findByTypeAndOsTypeOrderBySortAscCreateAsc(String type,String osType,Pageable pageable);
}