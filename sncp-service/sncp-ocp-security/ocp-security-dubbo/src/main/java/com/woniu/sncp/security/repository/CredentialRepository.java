package com.woniu.sncp.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.security.entity.CredentialEntity;

public interface CredentialRepository extends JpaRepository<CredentialEntity, String> {
	
	@Query("FROM CredentialEntity c where c.userName=:username and c.password=:password and credentialState='0'")
	public CredentialEntity findByUserName(@Param("username") String username,@Param("password") String password);
	
	public List<CredentialEntity> findByCredentialState(String credentialState);
}
