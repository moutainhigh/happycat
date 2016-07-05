package com.woniu.sncp.profile.service;

import java.util.List;

import com.woniu.sncp.profile.dto.CardValueDTO;
import com.woniu.sncp.profile.dto.CardDetailDTO;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface CardManageService {

	List<CardValueDTO> findValueByGameIdAndPlatformId(Long gameId,Long paymentId);
	
	List<CardDetailDTO> findDetailByGameIdAndPlatformId(Long gameId,Long paymentId);
}
