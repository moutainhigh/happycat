package com.woniu.sncp.cbss.api.profile.controller;

import org.springframework.util.ObjectUtils;

import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月5日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class GameConfRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long serverId;
	
	private String state="1";
	private String type="1";
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	/* (non-Javadoc)
	 * @see com.woniu.sncp.cbss.core.model.request.Param#checkParamValueIn()
	 */
	public boolean checkParamValueIn() throws ParamValueValidateException {
		if (ObjectUtils.isEmpty(getGameId()) && ObjectUtils.isEmpty(getServerId())) {
			return false;
		}
		return true;
	}

}
