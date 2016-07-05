package com.woniu.sncp.profile.service;

import java.util.Map;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface ActivityManageService {

	public Map<String,Object> findAllPloysByState(Long gameId,String state);
}
