package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;

public class ProfileDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	
	private String applicationName;
	
	private String company;
	
	private String companyTel;
	
	private String companyAddress;
	
	private String contacterName;
	
	private String contacterTel;
	
	private String contacterEmail;
	
	private String contacterIM;
	
	private Date createDate;
	
	private Date updateDate;
	
	private CredentialDTO credential;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCompanyTel() {
		return companyTel;
	}

	public void setCompanyTel(String companyTel) {
		this.companyTel = companyTel;
	}

	public String getCompanyAddress() {
		return companyAddress;
	}

	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}

	public String getContacterName() {
		return contacterName;
	}

	public void setContacterName(String contacterName) {
		this.contacterName = contacterName;
	}

	public String getContacterTel() {
		return contacterTel;
	}

	public void setContacterTel(String contacterTel) {
		this.contacterTel = contacterTel;
	}

	public String getContacterEmail() {
		return contacterEmail;
	}

	public void setContacterEmail(String contacterEmail) {
		this.contacterEmail = contacterEmail;
	}

	public String getContacterIM() {
		return contacterIM;
	}

	public void setContacterIM(String contacterIM) {
		this.contacterIM = contacterIM;
	}

	public CredentialDTO getCredential() {
		return credential;
	}

	public void setCredential(CredentialDTO credential) {
		this.credential = credential;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
