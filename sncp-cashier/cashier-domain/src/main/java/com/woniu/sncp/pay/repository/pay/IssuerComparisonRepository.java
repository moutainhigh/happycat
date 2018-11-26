package com.woniu.sncp.pay.repository.pay;



import java.util.List;

 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>descrption: 运营商对照表</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Repository
public interface IssuerComparisonRepository extends JpaRepository<IssuerComparison, IssuerComparisonKey> {
	@Query(value="from IssuerComparison  as t where t.id.issuerId=:issuerId and t.id.issuerMark=:issuerMark and t.state='1' ")
	public List<IssuerComparison> query(@Param("issuerId")Long issuerId,@Param("issuerMark")String  issuerMark);

}
