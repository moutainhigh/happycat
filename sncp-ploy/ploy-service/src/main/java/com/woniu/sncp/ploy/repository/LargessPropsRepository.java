package com.woniu.sncp.ploy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woniu.sncp.ploy.entity.LargessProps;

public interface LargessPropsRepository extends JpaRepository<LargessProps, Long> {

	@Query("select count(*) from LargessProps info where info.aid=:userId and info.gameId=:gameId and info.gameAreaId=:gameAreaId and info.relatedId in" + 
	"(select blog.id from PloyBusinessLog blog where blog.userId =:userId and blog.relatedId =:ployId)")
	public int findCountUnionPloyBusinessLogByUserIdAndGameIdAndGameAreaIdAndPloyId(@Param("userId") Long userId, @Param("gameId")  Long gameId, @Param("gameAreaId")  Long gameAreaId ,@Param("ployId")  Long ployId );
	
	@Query("select count(*) from LargessProps info where info.aid=:userId and info.gameId=:gameId and info.relatedId in" + 
			"(select blog.id from PloyBusinessLog blog where blog.userId =:userId and blog.relatedId =:ployId)")
	public int findCountUnionPloyBusinessLogByUserIdAndGameIdAndPloyId(@Param("userId") Long userId, @Param("gameId")  Long gameId, @Param("ployId")  Long ployId );
}
