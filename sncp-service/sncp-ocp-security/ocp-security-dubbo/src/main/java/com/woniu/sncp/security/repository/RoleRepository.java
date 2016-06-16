package com.woniu.sncp.security.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.security.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, String> {

}
