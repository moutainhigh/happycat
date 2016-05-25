package cbss.api.nciic.controller;

import cbss.core.model.request.ParamValueValidateException;
import cbss.core.model.request.RequestParam;

public class NciicRequestParam extends RequestParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7374406705116955637L;

	private String realName;
	private String identityNo;

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdentityNo() {
		return identityNo;
	}

	public void setIdentityNo(String identityNo) {
		this.identityNo = identityNo;
	}

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		return true;
	}

}
