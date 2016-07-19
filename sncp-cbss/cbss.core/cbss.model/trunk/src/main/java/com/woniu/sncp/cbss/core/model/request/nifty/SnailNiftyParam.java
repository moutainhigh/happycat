package com.woniu.sncp.cbss.core.model.request.nifty;

import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

public class SnailNiftyParam extends RequestParam{

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		return true;
	}

}
