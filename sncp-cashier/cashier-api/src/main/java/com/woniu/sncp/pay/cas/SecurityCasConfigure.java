package com.woniu.sncp.pay.cas;

import java.util.Arrays;
import java.util.List;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * descrption:
 * </p>
 * 
 * @author fuzl
 * @date 2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@ConfigurationProperties(prefix = "spring.cas")
public class SecurityCasConfigure {



	static final String separator = ",";
	private String validateFilters;
	private String signOutFilters;
	private String authFilters;
	private String assertionFilters;
	private String requestWrapperFilters;

	private String casServerUrlPrefix;
	private String casServerLoginUrl;
	private String encoding;
	private String casServerMLoginUrl;
	private String serverName;
	private String 	service;
	private boolean useSession = true;
	private boolean redirectAfterValidation = true;

	public List<String> getValidateFilters() {
		return Arrays.asList(validateFilters.split(separator));
	}

	public void setValidateFilters(String validateFilters) {
		this.validateFilters = validateFilters;
	}

	public List<String> getSignOutFilters() {
		return Arrays.asList(signOutFilters.split(separator));
	}

	public void setSignOutFilters(String signOutFilters) {
		this.signOutFilters = signOutFilters;
	}

	public List<String> getAuthFilters() {
		return Arrays.asList(authFilters.split(separator));
	}

	public void setAuthFilters(String authFilters) {
		this.authFilters = authFilters;
	}

	public List<String> getAssertionFilters() {
		return Arrays.asList(assertionFilters.split(separator));
	}

	public void setAssertionFilters(String assertionFilters) {
		this.assertionFilters = assertionFilters;
	}

	public List<String> getRequestWrapperFilters() {
		return Arrays.asList(requestWrapperFilters.split(separator));
	}

	public void setRequestWrapperFilters(String requestWrapperFilters) {
		this.requestWrapperFilters = requestWrapperFilters;
	}

	public String getCasServerUrlPrefix() {
		return casServerUrlPrefix;
	}

	public void setCasServerUrlPrefix(String casServerUrlPrefix) {
		this.casServerUrlPrefix = casServerUrlPrefix;
	}

	public String getCasServerLoginUrl() {
		return casServerLoginUrl;
	}

	public void setCasServerLoginUrl(String casServerLoginUrl) {
		this.casServerLoginUrl = casServerLoginUrl;
	}
	
	public String getCasServerMLoginUrl() {
		return casServerMLoginUrl;
	}

	public void setCasServerMLoginUrl(String casServerMLoginUrl) {
		this.casServerMLoginUrl = casServerMLoginUrl;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isRedirectAfterValidation() {
		return redirectAfterValidation;
	}

	public void setRedirectAfterValidation(boolean redirectAfterValidation) {
		this.redirectAfterValidation = redirectAfterValidation;
	}

	public boolean isUseSession() {
		return useSession;
	}

	public void setUseSession(boolean useSession) {
		this.useSession = useSession;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}


}
