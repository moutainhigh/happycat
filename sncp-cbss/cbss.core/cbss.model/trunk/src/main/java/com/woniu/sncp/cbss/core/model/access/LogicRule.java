package com.woniu.sncp.cbss.core.model.access;

import java.util.List;

public class LogicRule {

	private List<ParamLogic> IF;
	private List<Limit> T;
	private List<ParamLogicFALSE> F;

	public List<ParamLogic> getIF() {
		return IF;
	}

	public void setIF(List<ParamLogic> iF) {
		IF = iF;
	}

	public List<Limit> getT() {
		return T;
	}

	public void setT(List<Limit> t) {
		T = t;
	}

	public List<ParamLogicFALSE> getF() {
		return F;
	}

	public void setF(List<ParamLogicFALSE> f) {
		F = f;
	}


}
