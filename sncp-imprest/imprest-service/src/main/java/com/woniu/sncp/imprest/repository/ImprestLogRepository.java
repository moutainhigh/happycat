package com.woniu.sncp.imprest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.imprest.entity.ImprestLog;

public interface ImprestLogRepository extends JpaRepository<ImprestLog, Long>{
	
}
