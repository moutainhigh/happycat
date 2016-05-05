package com.woniu.sncp.passport.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 帐号返回参数
 * @author chenyx
 * @date 2016年5月4日
 */
public class PassportDto implements Serializable {

	private static final long serialVersionUID = 6473223887162527457L;

	private Long id;

	/**
	 * 帐号 
	 */
	private String account;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * VIP级别 
	 */
	private String vipLevel;

	/**
	 * 真实姓名 
	 */
	private String name;

	/**
	 * 身份证号码 
	 */
	private String identity;

	/**
	 * 身份证验证状态 
	 */
	private String identityAuthState;

	/**
	 * 年龄标志 - S_FLAG
	 */
	private String ageFlag;

	/**
	 * 性别 - S_GENDER
	 */
	private String gender;

	/**
	 * 身份证生日 - D_IDEN_BIRTH
	 */
	private Date identityBirthday;

	/**
	 * 生日 - D_BIRTH
	 */
	private Date birthday;

	/**
	 * EMAIL - S_EMAIL
	 */
	private String email;

	/**
	 * 帐号状态 - S_STATE
	 */
	private String state;

	/**
	 * 注册日期 - D_CREATE
	 */
	private Date createDate;

	/**
	 * 注册时IP - N_IP
	 */
	private Long regIp;

	/**
	 * 注册城市 - S_IP_CITY
	 */
	private Long regCityId;

	/**
	 * 区域运营商ID - N_ISSUER_ID
	 */
	private Long issuerId;

	/**
	 * 区域二级推广商ID - N_SPREADER_ID
	 */
	private Long spreaderId;
	
	/**
	 * EMAIL是否通过认证 - S_EMAIL_AUTHED
	 */
	private String emailAuthed;

	/**
	 * 绑定手机 - S_MOBILE
	 */
	private String mobile;
	
	/**
	 * 手机是否通过认证 - S_MOBILE_AUTHED
	 */
	private String mobileAuthed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getIdentityAuthState() {
		return identityAuthState;
	}

	public void setIdentityAuthState(String identityAuthState) {
		this.identityAuthState = identityAuthState;
	}

	public String getAgeFlag() {
		return ageFlag;
	}

	public void setAgeFlag(String ageFlag) {
		this.ageFlag = ageFlag;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getIdentityBirthday() {
		return identityBirthday;
	}

	public void setIdentityBirthday(Date identityBirthday) {
		this.identityBirthday = identityBirthday;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getRegIp() {
		return regIp;
	}

	public void setRegIp(Long regIp) {
		this.regIp = regIp;
	}

	public Long getRegCityId() {
		return regCityId;
	}

	public void setRegCityId(Long regCityId) {
		this.regCityId = regCityId;
	}

	public Long getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

	public Long getSpreaderId() {
		return spreaderId;
	}

	public void setSpreaderId(Long spreaderId) {
		this.spreaderId = spreaderId;
	}

	public String getEmailAuthed() {
		return emailAuthed;
	}

	public void setEmailAuthed(String emailAuthed) {
		this.emailAuthed = emailAuthed;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMobileAuthed() {
		return mobileAuthed;
	}

	public void setMobileAuthed(String mobileAuthed) {
		this.mobileAuthed = mobileAuthed;
	}
	
}
