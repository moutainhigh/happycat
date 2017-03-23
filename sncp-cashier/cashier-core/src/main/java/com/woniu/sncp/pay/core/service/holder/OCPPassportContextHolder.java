package com.woniu.sncp.pay.core.service.holder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.woniu.sncp.ocp.utils.OCPContextHolder;
import com.woniu.sncp.web.IpUtils;

/**
 * ocp核心计费上下文
 * @author chenyx
 *
 */
@Service
public class OCPPassportContextHolder extends OCPContextHolder {

	@Value("${core.passport.app.id}")
	private String appId;
	
	@Value("${core.passport.app.pwd}")
	private String appPwd;
	
	@Value("${core.passport.version}")
	private String version;
	
	@Value("${core.passport.server}")
	private String server;
	
	@Value("${core.passport.cbc}")
    private String cbc;
	
	@Value("${core.passport.connect.timeout}")
    private String conTimeOut;

    @Value("${core.passport.read.timeout}")
    private String readTimeOut;

    public String getCbc() {
		return cbc;
	}

	public void setCbc(String cbc) {
		this.cbc = cbc;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppPwd() {
		return appPwd;
	}

	public void setAppPwd(String appPwd) {
		this.appPwd = appPwd;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}


	/**
	 * 返回服务器IP地址
	 * @return
	 */
	public String getServerIP() {
		return getRequest().getLocalAddr();
	}
	
	/**
	 * 返回客户端IP地址
	 * @return
	 */
	public String getClientIP() {
		return IpUtils.getRemoteAddr(getRequest());
	}

    public String getConTimeOut() {
        return conTimeOut;
    }

    public void setConTimeOut(String conTimeOut) {
        this.conTimeOut = conTimeOut;
    }

    public String getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(String readTimeOut) {
        this.readTimeOut = readTimeOut;
    }
	
}
