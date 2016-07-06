package com.woniu.sncp.cbss.api.profile.controller;


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
public class ActivityConfRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private String state="3";
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	

	/* (non-Javadoc)
	 * @see com.woniu.sncp.cbss.core.model.request.Param#checkParamValueIn()
	 */
	public boolean checkParamValueIn() throws ParamValueValidateException {
		return true;
	}

}
