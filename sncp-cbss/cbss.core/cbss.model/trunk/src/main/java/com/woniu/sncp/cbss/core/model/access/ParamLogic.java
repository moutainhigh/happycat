package com.woniu.sncp.cbss.core.model.access;

import org.apache.commons.lang.StringUtils;

public class ParamLogic {
	private String expr;

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public String[] pnames() {
		String[] names = StringUtils.substringsBetween(this.expr, "[", "]");
		return names;
	}

}
