package com.woniu.sncp.pay.core.security;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;



public class SecuritySSOAuth{
	
	public static Long getLoginId(){
		Assertion assertion = org.jasig.cas.client.util.AssertionHolder
				.getAssertion();
		if (assertion != null) {
			AttributePrincipal ap = assertion.getPrincipal();
			java.util.Map<String, Object> map = ap.getAttributes();
			return Long.valueOf(String.valueOf(map.get("naid")));
		}
		return null;
	}
	
	public  String shortMobile(String mobile) {
		if(org.apache.commons.lang.StringUtils.isNotEmpty(mobile) && mobile.length() == 11) {
			return mobile.substring(0, 3) + "******" + mobile.substring(9);
		}
		return mobile;
	}

}
