package com.woniu.sncp.profile.dao;

import java.util.List;

import com.woniu.sncp.profile.po.CardValuePo;
import com.woniu.sncp.profile.po.CardDetailPo;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface CardManageDao {

	List<CardValuePo> findValueByGameIdAndPlatformId(Long gameId,Long platformId);
	
	List<CardDetailPo> findDetailByGameIdAndPlatformId(Long gameId,Long platformId);
}
