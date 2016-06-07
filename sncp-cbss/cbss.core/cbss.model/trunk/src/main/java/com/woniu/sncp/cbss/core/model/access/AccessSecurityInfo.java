/**
 * 
 */
package com.woniu.sncp.cbss.core.model.access;

import java.util.Date;

/**
 * 
 * 
 * 目前用于对外提供的服务进行安全信息验证 如webserice,http接口访问前公用验证信息
 * 
 * @author maocc
 * 
 */
public class AccessSecurityInfo{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String S_TYPE_SNAIL_COMMON = "1";// 不限方法\限IP\限校验串
	public static final String S_TYPE_UNION_SEP = "2";// 限方法\限IP\限校验串
	public static final String S_TYPE_UNION_MIX = "3";// 限方法\限IP\限校验串
	public static final String S_TYPE_SNAIL_JWL = "4";// 限方法\限IP\限校验串
	public static final String S_TYPE_SNAIL_DIRECT_IMPREST = "5";// 限方法\限IP\限校验串
	public static final String S_TYPE_SNAIL_HOST_GAME = "6";// 限方法\不限IP\限校验串
	public static final String S_TYPE_SNAIL_HOST_INTERFACE = "7";// 限方法\限IP\不限校验串
	public static final String S_TYPE_SNAIL_HOST_INTERFACE_NOIP_NOVAIL = "8";// 限方法\不限IP\不限校验串
	public static final String S_TYPE_SNAIL_HOST_INTERFACE_IP_VAIL = "9";// 官方接口(限方法限IP校验串)

	public static final String S_STATE_USED = "1";
	public static final String S_STATE_UN_USED = "0";
	public static final String S_STATE_LIMITED_ACCOUNT_USED = "2";
	/**
	 * 复合主键
	 */
	private AccessSecurityInfoKey id;

	/**
	 * 
	 */
	private String name;

	/**
	 * 
	 */
	private String fullName;

	/**
	 * 
	 */
	private String linker;

	/**
	 * 
	 */
	private String tel;

	/**
	 * 
	 */
	private String email;

	/**
	 * 
	 */
	private String im;

	/**
	 * 
	 */
	private String prefix;

	/**
	 * 
	 */
	private String seed;

	/**
	 * 
	 */
	private String passwd;

	/**
	 * 
	 */
	private String limitAccount;

	/**
	 * 
	 */
	private String limitIp;

	/**
	 * 
	 */
	private Date start;

	/**
	 * 
	 */
	private Date end;

	/**
	 * 
	 */
	private Date dateCreate;
	/**
	 * 
	 */
	private String state;
	/**
	 * 
	 */
	private String note;

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getLinker() {
		return linker;
	}

	public String getTel() {
		return tel;
	}

	public String getEmail() {
		return email;
	}

	public String getIm() {
		return im;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSeed() {
		return seed;
	}

	public String getPasswd() {
		return passwd;
	}

	public String getLimitIp() {
		return limitIp;
	}

	public String getState() {
		return state;
	}

	public String getNote() {
		return note;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setLinker(String linker) {
		this.linker = linker;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setIm(String im) {
		this.im = im;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public void setLimitIp(String limitIp) {
		this.limitIp = limitIp;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public AccessSecurityInfoKey getId() {
		return id;
	}

	public void setId(AccessSecurityInfoKey id) {
		this.id = id;
	}

	public String getLimitAccount() {
		return limitAccount;
	}

	public void setLimitAccount(String limitAccount) {
		this.limitAccount = limitAccount;
	}

	public Date getDateCreate() {
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate) {
		this.dateCreate = dateCreate;
	}

}
