package com.woniu.sncp.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.security.entity.AuthorityEntity;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, String>{
	
	public AuthorityEntity findByAuthorityName(String authorityName);
}
