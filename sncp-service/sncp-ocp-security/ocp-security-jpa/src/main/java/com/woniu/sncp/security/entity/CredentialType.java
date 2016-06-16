package com.woniu.sncp.security.entity;

public enum CredentialType {
	
	USER("0", "User"),
	ADMIN("1", "Administrator");
	
	private String code;
	private String name;
	
	private CredentialType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return code;
	}
	
}
