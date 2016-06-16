package com.woniu.sncp.security.entity;

public enum ActiveState {
	
	ACTIVED("0", "Actived"),
	FROZEN("1", "Frozen"),
	ACCOUNT_EXPIRED("2", "Account Expired"),
	CREDENTIALS_EXPIRED("3", "Password Expired"),
	ACCOUNT_LOCKED("4", "Account Locked");
	
	private String code;
	private String description;
	
	private ActiveState(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return this.getCode();
	}
}
