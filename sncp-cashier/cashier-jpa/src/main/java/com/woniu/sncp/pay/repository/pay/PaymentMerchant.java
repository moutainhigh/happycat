package com.woniu.sncp.pay.repository.pay;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * <p>descrption: 收银台支付业务申请表</p>
 * 
 * @author fuzl
 * @date   2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Entity
@Table(name = "PAY_MERCHANT", schema = "SN_PAY")
public class PaymentMerchant implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2135152699794408099L;
	
	public static final String STATUS_VALID = "1";
	/**
	 * 支付业务申请号
	 */
	@Id
    @Column(name = "N_ID")
	private Long id;
//	/**
//	 * 支付业务申请号
//	 */
//	private long merchantId;
	/**
	 * 游戏id
	 */
	private long gameId;
	/**
	 * 支付业务名称
	 */
	private String name;
	/**
	 * 申请人邮箱地址
	 */
	private String merchantEmail;
	/**
	 * 申请人联系人
	 */
	private String contact;
	/**
	 * 申请人联系人电话
	 */
	private String contactPhone;
	/**
	 * 密钥类型
	 */
	private String keyType;
	/**
	 * Md5密钥或商户公钥或DES密钥串
	 */
	private String merchantKey;
	/**
	 * 我方私钥（md5无私钥)
	 */
	private String privateKey;
	/**
	 * 我方公钥（md5无公钥)
	 */
	private String publicKey;
	/**
	 * 状态
	 */
	private String status;
	/**
	 * 申请时间
	 */
	private Date create;
	
	private String limitIp;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	//	public long getMerchantId() {
//		return merchantId;
//	}
//	public void setMerchantId(long merchantId) {
//		this.merchantId = merchantId;
//	}
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMerchantEmail() {
		return merchantEmail;
	}
	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getKeyType() {
		return keyType;
	}
	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
	public String getMerchantKey() {
		return merchantKey;
	}
	public void setMerchantKey(String merchantKey) {
		this.merchantKey = merchantKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreate() {
		return create;
	}
	public void setCreate(Date create) {
		this.create = create;
	}
	public String getLimitIp() {
		return limitIp;
	}
	public void setLimitIp(String limitIp) {
		this.limitIp = limitIp;
	}
}
