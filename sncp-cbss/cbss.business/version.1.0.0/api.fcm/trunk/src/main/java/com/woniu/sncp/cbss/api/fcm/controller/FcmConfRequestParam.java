package com.woniu.sncp.cbss.api.fcm.controller;

import org.apache.commons.lang.StringUtils;

import com.woniu.sncp.cbss.core.authorize.nifty.NiftyParam;
import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

@NiftyParam
public class FcmConfRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8067535783484572183L;

	private String gameIds;

	public String getGameIds() {
		return gameIds;
	}

	public void setGameIds(String gameIds) {
		this.gameIds = gameIds;
	}

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		String gameIds = getGameIds();
		if(StringUtils.isBlank(gameIds)){
			return false;
		}
		Long issuerId = getIssuerId();
		if(issuerId == null || issuerId.longValue() <= 0L){
			return false;
		}
		return true;
	}
}
