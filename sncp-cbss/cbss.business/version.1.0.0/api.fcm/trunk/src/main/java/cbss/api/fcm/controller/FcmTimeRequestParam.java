package cbss.api.fcm.controller;

import cbss.core.model.request.ParamValueValidateException;
import cbss.core.model.request.RequestParam;

public class FcmTimeRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8067535783484572183L;

	private Long leaveTime;
	
	private Long time;

	public Long getLeaveTime() {
		return leaveTime;
	}

	public void setLeaveTime(Long leaveTime) {
		this.leaveTime = leaveTime;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		Long leaveTime = getLeaveTime();
		if(leaveTime != null && leaveTime.longValue() < 0L){
			return false;
		}
		Long time = getTime();
		if(time != null && time.longValue() < 0L){
			return false;
		}
		Long aid = getAid();
		if(aid == null || aid.longValue() <= 0L){
			return false;
		}
		Long gameId = getGameId();
		if(gameId == null || gameId.longValue() <= 0L){
			return false;
		}
		return true;
	}
}
