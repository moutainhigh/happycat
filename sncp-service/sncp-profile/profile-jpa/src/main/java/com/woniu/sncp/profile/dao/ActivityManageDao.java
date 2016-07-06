package com.woniu.sncp.profile.dao;

import java.util.List;

import com.woniu.sncp.profile.po.PassportPresentsPloyDetailPo;
import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface ActivityManageDao {

	public List<PassportPresentsPloyPo> findAllByStateAndPloyTypes(String state,List<String> ployTypes);
	
	public List<PassportPresentsPloyDetailPo> findAllByStateAndPloyIds(List<Integer> ployIds);
}
