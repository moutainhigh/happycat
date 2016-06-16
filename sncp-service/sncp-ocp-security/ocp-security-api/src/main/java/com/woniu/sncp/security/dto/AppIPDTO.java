package com.woniu.sncp.security.dto;

import java.io.Serializable;

public class AppIPDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String ip;
	
	private CredentialDTO credential;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public CredentialDTO getCredential() {
		return credential;
	}

	public void setCredential(CredentialDTO credential) {
		this.credential = credential;
	}
}
