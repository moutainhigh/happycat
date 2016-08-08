package com.woniu.sncp.ploy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.ploy.entity.PassportPresentsPloyDetail;

public interface PassportPresentsPloyDetailRepository extends JpaRepository<PassportPresentsPloyDetail, Long> {
	
	/**
	 * 根据活动ID查询活动明细
	 * @param ployId
	 * @return
	 */
	public List<PassportPresentsPloyDetail> findByPloyId(Long ployId);
	

}
