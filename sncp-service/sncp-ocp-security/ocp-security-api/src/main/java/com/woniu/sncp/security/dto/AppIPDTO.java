package com.woniu.sncp.security.dto;

import java.io.Serializable;

public class AppIPDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String ip;
	
	private CredentialDTO credential;
	
	public AppIPDTO() {
	}

	public AppIPDTO(String ip) {
		this.ip = ip;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppIPDTO other = (AppIPDTO) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		return true;
	}
	
}
