package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class Authentication {
	private String publicKey;
	private String token;
	private String hash;
	
	public Authentication() {
	}
	public Authentication(String publicKey, String token, String hash) {
		super();
		this.publicKey = publicKey;
		this.token = token;
		this.hash = hash;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
}