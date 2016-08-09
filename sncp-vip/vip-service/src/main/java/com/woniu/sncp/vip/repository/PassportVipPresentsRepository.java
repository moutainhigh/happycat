package com.woniu.sncp.vip.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.vip.entity.PassportVipPresents;
import com.woniu.sncp.vip.entity.PassportVipPresentsPK;

public interface PassportVipPresentsRepository extends JpaRepository<PassportVipPresents, PassportVipPresentsPK> {

	@Query("from PassportVipPresents vp where vp.id.sendLevel = :sendLevel")
	public Page<PassportVipPresents> findBySendLevel(@Param("sendLevel") String sendLevel, Pageable pageable);
}
