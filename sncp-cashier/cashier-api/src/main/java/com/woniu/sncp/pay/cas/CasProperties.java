package com.woniu.sncp.pay.cas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Component
public class CasProperties {

	@Value("${cas.server.url}")
    private String casServerUrl;

    @Value("${cas.server.login_url}")
    private String casServerLoginUrl;
    
    @Value("${cas.server.m_login_url}")
    private String casServerMLoginUrl;

    @Value("${cas.server.logout_url}")
    private String casServerLogoutUrl;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.login_url}")
    private String appLoginUrl;

    @Value("${app.logout_url}")
    private String appLogoutUrl;

    public String getCasServerUrl() {
        return casServerUrl;
    }

    public void setCasServerUrl(String casServerUrl) {
        this.casServerUrl = casServerUrl;
    }

    public String getCasServerLoginUrl() {
        return casServerLoginUrl;
    }

    public void setCasServerLoginUrl(String casServerLoginUrl) {
        this.casServerLoginUrl = casServerLoginUrl;
    }

    public String getCasServerLogoutUrl() {
        return casServerLogoutUrl;
    }

    public void setCasServerLogoutUrl(String casServerLogoutUrl) {
        this.casServerLogoutUrl = casServerLogoutUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getAppLoginUrl() {
        return appLoginUrl;
    }

    public void setAppLoginUrl(String appLoginUrl) {
        this.appLoginUrl = appLoginUrl;
    }

    public String getAppLogoutUrl() {
        return appLogoutUrl;
    }

    public void setAppLogoutUrl(String appLogoutUrl) {
        this.appLogoutUrl = appLogoutUrl;
    }
    
    public String getCasServerMLoginUrl() {
		return casServerMLoginUrl;
	}

	public void setCasServerMLoginUrl(String casServerMLoginUrl) {
		this.casServerMLoginUrl = casServerMLoginUrl;
	}

	@Override
	public String toString() {
		return "CasProperties [casServerUrl=" + casServerUrl + ", casServerLoginUrl=" + casServerLoginUrl
				+ ", casServerMLoginUrl=" + casServerMLoginUrl + ", casServerLogoutUrl=" + casServerLogoutUrl
				+ ", appUrl=" + appUrl + ", appLoginUrl=" + appLoginUrl + ", appLogoutUrl=" + appLogoutUrl + "]";
	}

}
