package com.woniu.sncp.ploy.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.ploy.entity.PresentsPloy;

public interface PresentsPloyRepository extends JpaRepository<PresentsPloy, Long> {

	@Query("SELECT p FROM PresentsPloy p WHERE p.limitGame like %:gameId% AND p.state = '1' AND "
			+ "((p.start <= :currentDate AND p.end >= :currentDate) OR ((p.start is null) AND p.end >= :currentDate) "
			+ "OR (p.start <= :currentDate AND (p.end is null)) OR ((p.start is null) AND (p.end is null)))")
	public List<PresentsPloy> findByLimitGameAndState(@Param("gameId") String gameId,
			@Param("currentDate") Date currentDate);
}
