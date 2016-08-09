package com.woniu.sncp.vip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.vip.entity.PassportVip;
import com.woniu.sncp.vip.entity.PassportVipPK;

/**
 * 帐号vip
 * @author chenyx
 *
 */
public interface PassportVipRepository extends JpaRepository<PassportVip, PassportVipPK> {

	public PassportVip findById(PassportVipPK id);
}
