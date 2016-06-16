package com.woniu.sncp.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.security.entity.ResourceEntity;

public interface ResourceRepository extends JpaRepository<ResourceEntity, String> {

	public List<ResourceEntity> findByEnabled(String enabled);
}
