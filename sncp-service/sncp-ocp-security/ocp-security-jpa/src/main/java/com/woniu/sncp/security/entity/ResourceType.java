package com.woniu.sncp.security.entity;

public enum ResourceType {
	
	METHOD("0", "Method"),
	URL("1", "URL");
	
	private String code;
	private String name;
	
	private ResourceType(String code, String name) {
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
		return this.code;
	}
	
}
