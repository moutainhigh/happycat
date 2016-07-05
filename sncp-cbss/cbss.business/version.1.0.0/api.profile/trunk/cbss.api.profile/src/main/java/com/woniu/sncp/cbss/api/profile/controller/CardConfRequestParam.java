package com.woniu.sncp.cbss.api.profile.controller;

import org.springframework.util.ObjectUtils;

import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月4日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class CardConfRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long platformId;
	
	public Long getPlatformId() {
		return platformId;
	}



	public void setPlatformId(Long platformId) {
		this.platformId = platformId;
	}



	/* (non-Javadoc)
	 * @see com.woniu.sncp.cbss.core.model.request.Param#checkParamValueIn()
	 */
	public boolean checkParamValueIn() throws ParamValueValidateException {
		if (ObjectUtils.isEmpty(getGameId()) || ObjectUtils.isEmpty(getPlatformId())) {
			return false;
		}
		return true;
	}

}
