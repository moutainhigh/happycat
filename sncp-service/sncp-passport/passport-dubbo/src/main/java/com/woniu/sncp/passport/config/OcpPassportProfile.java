package com.woniu.sncp.passport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ocp", ignoreUnknownFields=true)
public class OcpPassportProfile {
	
	/**
	 * 远程调用超时时间
	 */
	private int connectTimeout;
	
	/**
	 * 远程调用返回超时时间
	 */
	private int readTimeout;
	
	/**
	 * ocp远程调用帐号
	 */
	private String appid;
	
	/**
	 * ocp远程调用帐号密码
	 */
	private String pwd;

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
