package com.woniu.sncp.pay.repository.pay;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class IssuerComparisonKey implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * 运营商ID
	 */
	@Column(name = "N_ISSUER_ID")
	private Long issuerId;

	/**
	 * 类型
	 */
	@Column(name = "S_TYPE")
	private String type;

	/**
	 * 运营商标识
	 */
	@Column(name = "N_ISSUER_MARK")
	private String issuerMark;

	public Long getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIssuerMark() {
		return issuerMark;
	}

	public void setIssuerMark(String issuerMark) {
		this.issuerMark = issuerMark;
	}
}
