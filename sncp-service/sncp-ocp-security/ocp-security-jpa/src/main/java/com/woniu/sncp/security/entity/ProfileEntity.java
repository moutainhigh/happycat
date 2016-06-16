package com.woniu.sncp.security.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="T_ACL_PROFILE", schema="SN_ACL")
public class ProfileEntity implements Serializable {

	private static final long serialVersionUID = -3048062656513315972L;
	
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 第三方应用程序名称
	 */
	@Column(name="S_APP_NAME", nullable=false)
	private String appName;
	
	/**
	 * 第三方应用程序所属开发公司
	 */
	@Column(name="S_COMPANY_NAME")
	private String companyName;
	
	/**
	 * 第三方应用程序所属公司电话
	 */
	@Column(name="S_COMPANY_TEL")
	private String companyTel;
	
	/**
	 * 第三方应用程序开发公司地址
	 */
	@Column(name="S_ADDRESS")
	private String companyAddress;
	
	/**
	 * 联系人
	 */
	@Column(name="S_CONTACTER_NAME")
	private String contacterName;
	
	/**
	 * 联系人电话
	 */
	@Column(name="S_CONTACTER_TEL")
	private String contacterTel;
	
	/**
	 * 联系人邮件地址
	 */
	@Column(name="S_CONTACTER_EMAIL")
	private String contacterEmail;
	
	/**
	 * 联系人即时通讯软件号码
	 */
	@Column(name="S_CONTACTER_IM")
	private String contacterIM;
	
	/**
	 * 创建时间
	 */
	@Column(name="D_CREATE_DATE", nullable=false)
	private Date createDate;
	
	/**
	 * 更新时间
	 */
	@Column(name="D_UPDATE_DATE", nullable=false)
	private Date updateDate;
	
	/**
	 * 用户帐号
	 */
	@OneToOne(optional=false, cascade={CascadeType.REFRESH, CascadeType.MERGE}, fetch=FetchType.LAZY)
	@JoinColumn(name="S_CREDENTIAL_ID", nullable=false)
	private CredentialEntity credential;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public CredentialEntity getCredential() {
		return credential;
	}

	public void setCredential(CredentialEntity credential) {
		this.credential = credential;
	}

}
