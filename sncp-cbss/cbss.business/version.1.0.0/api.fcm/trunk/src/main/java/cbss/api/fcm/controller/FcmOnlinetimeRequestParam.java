package cbss.api.fcm.controller;

import cbss.core.model.request.ParamValueValidateException;
import cbss.core.model.request.RequestParam;

public class FcmOnlinetimeRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5608463115672404174L;

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		Long aid = getAid();
		if(aid == null || aid.longValue() <= 0L){
			return false;
		}
		Long issuerId = getIssuerId();
		if(issuerId == null || issuerId.longValue() <= 0L){
			return false;
		}
		Long gameId = getGameId();
		if(gameId == null || gameId.longValue() <= 0L){
			return false;
		}
		return true;
	}
}
