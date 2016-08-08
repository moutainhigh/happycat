package com.woniu.sncp.imprest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woniu.sncp.imprest.entity.ImprestOrder;

public interface ImprestOrderRepository extends JpaRepository<ImprestOrder, Long>{

	public ImprestOrder findImprestOrderByOrderNoAndGameAreaIdAndAid(String orderNo, Long gameAreaId, Long aid);
}
