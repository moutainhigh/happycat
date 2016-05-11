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
	
	private String aliase;

	
	/**
	 * @return 帐号ID
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id 帐号ID
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return 蜗牛通行证
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * @param account 蜗牛通行证
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @return 加密后密码
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password 加密后密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return vip级别,用于区分密码加密方式
	 * 普通帐号 - 0
	 * 进过余数处理后的密码 - 1
	 */
	public String getVipLevel() {
		return vipLevel;
	}

	/**
	 * @param vipLevel vip级别,用于区分密码加密方式
	 * 普通帐号 - 0
	 * 进过余数处理后的密码 - 1
	 * 
	 */
	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	/**
	 * @return 真实姓名
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name 真实姓名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return 身份证号码
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * @param identity 身份证号码
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * @return 防沉迷状态 
	 * 未验证 - 1
	 * 验证中 - 2
	 * 验证通过 - 3
	 * 信息不规范 - 0
	 */
	public String getIdentityAuthState() {
		return identityAuthState;
	}

	/**
	 * @param identityAuthState 防沉迷状态 
	 * 未验证 - 1
	 * 验证中 - 2
	 * 验证通过 - 3
	 * 信息不规范 - 0
	 */
	public void setIdentityAuthState(String identityAuthState) {
		this.identityAuthState = identityAuthState;
	}

	/**
	 * @return 年龄标识
	 * 信息不规范 - 0
	 * 未成年 - 1
	 * 已成年 - 2
	 */
	public String getAgeFlag() {
		return ageFlag;
	}

	/**
	 * @param ageFlag 年龄标识
	 * 信息不规范 - 0
	 * 未成年 - 1
	 * 已成年 - 2
	 */
	public void setAgeFlag(String ageFlag) {
		this.ageFlag = ageFlag;
	}

	/**
	 * @return 性别 
	 * 男 - 1
	 * 女 - 0 
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender 性别 
	 * 男 - 1
	 * 女 - 0 
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return 身份证生日
	 */
	public Date getIdentityBirthday() {
		return identityBirthday;
	}

	/**
	 * @param identityBirthday 身份证生日
	 */
	public void setIdentityBirthday(Date identityBirthday) {
		this.identityBirthday = identityBirthday;
	}

	/**
	 * @return 生日
	 */
	public Date getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday 生日
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return 邮箱
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email 邮箱
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return 帐号状态
	 * 正常 - 1
	 * 锁定 - 2
	 * 转服中--3
	 * 冻结(停封) - 9
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state 帐号状态
	 * 正常 - 1
	 * 锁定 - 2
	 * 转服中--3
	 * 冻结(停封) - 9
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return 注册时间
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate 注册时间
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return 注册IP
	 */
	public Long getRegIp() {
		return regIp;
	}

	/**
	 * @param regIp 注册IP
	 */
	public void setRegIp(Long regIp) {
		this.regIp = regIp;
	}

	/**
	 * @return 城市ID
	 */
	public Long getRegCityId() {
		return regCityId;
	}

	/**
	 * @param regCityId 城市ID
	 */
	public void setRegCityId(Long regCityId) {
		this.regCityId = regCityId;
	}

	/**
	 * @return 运营商ID
	 */
	public Long getIssuerId() {
		return issuerId;
	}

	
	/**
	 * @param issuerId 运营商ID
	 */
	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

	/**
	 * @return 区域二级推广商ID
	 */
	public Long getSpreaderId() {
		return spreaderId;
	}

	/**
	 * @param spreaderId 区域二级推广商ID
	 */
	public void setSpreaderId(Long spreaderId) {
		this.spreaderId = spreaderId;
	}

	/**
	 * @return 邮箱验证状态 
	 * 0 - 未验证
	 * 1 - 已验证
	 */
	public String getEmailAuthed() {
		return emailAuthed;
	}

	/**
	 * @param emailAuthed 邮箱验证状态 
	 * 0 - 未验证
	 * 1 - 已验证
	 */
	public void setEmailAuthed(String emailAuthed) {
		this.emailAuthed = emailAuthed;
	}

	/**
	 * @return 手机
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile 手机
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return 手机验证状态
	 * 0 - 未验证
	 * 1 - 已验证
	 */
	public String getMobileAuthed() {
		return mobileAuthed;
	}

	/**
	 * @param mobileAuthed 手机验证状态
	 * 0 - 未验证
	 * 1 - 已验证
	 */
	public void setMobileAuthed(String mobileAuthed) {
		this.mobileAuthed = mobileAuthed;
	}

	/**
	 * @return 别名或者蜗牛虚商号码
	 */
	public String getAliase() {
		return aliase;
	}

	/**
	 * @param aliase 别名或者蜗牛虚商号码
	 */
	public void setAliase(String aliase) {
		this.aliase = aliase;
	}

	@Override
	public String toString() {
		return "PassportDto [id=" + id + ", account=" + account + ", password=" + password + ", vipLevel=" + vipLevel
				+ ", name=" + name + ", identity=" + identity + ", identityAuthState=" + identityAuthState
				+ ", ageFlag=" + ageFlag + ", gender=" + gender + ", identityBirthday=" + identityBirthday
				+ ", birthday=" + birthday + ", email=" + email + ", state=" + state + ", createDate=" + createDate
				+ ", regIp=" + regIp + ", regCityId=" + regCityId + ", issuerId=" + issuerId + ", spreaderId="
				+ spreaderId + ", emailAuthed=" + emailAuthed + ", mobile=" + mobile + ", mobileAuthed=" + mobileAuthed
				+ ", aliase=" + aliase + "]";
	}
	
	
	
}
