package com.woniu.sncp.security.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="T_ACL_LEGAL_IP", schema="SN_ACL")
public class ApplicationIPAddressEntity implements Serializable {

	private static final long serialVersionUID = -3070623664079922865L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;

	/**
	 * 可以访问平台服务的合法IP地址
	 */
	@Column(name="S_IP_ADDRESS", nullable=false)
	private String ip;
	
	/**
	 * 应用IP地址所属帐号
	 */
	@ManyToOne(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY)
	@JoinColumn(name="S_CREDENTIAL_ID", nullable=false)
	private CredentialEntity credential;

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

	public CredentialEntity getCredential() {
		return credential;
	}

	public void setCredential(CredentialEntity credential) {
		this.credential = credential;
	}
}
