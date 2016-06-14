package com.woniu.snco.passport.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



/**
 * 通行证帐号表 - SN_PASSPORT.PP_PASSPORT
 * 
 * @author chenyx
 * @since 1.0
 */
@Entity
@Table(name = "PP_PASSPORT", schema = "SN_PASSPORT")
public class PassportEntity implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	/**
	 * 密码加密级别
	 * @author chenyx
	 *
	 */
	public enum VipLevel {
		/**
		 * 普通帐号 - 0
		 */
		NORMAL {
			@Override
			public String toString() {
				return "0";
			}
		},
		/**
		 * 进过余数处理后的密码
		 */
		PWD_TYPE_MOD {
			@Override
			public String toString() {
				return "1";
			}
		}
	}
	
	public enum EpsStatus {
		/**
		 * 未验证 - 1
		 */
		NO_AUTH {
			@Override
			public String toString() {
				return "1";
			}
		},
		/**
		 * 验证中 - 2
		 */
		AUTH_ING {
			@Override
			public String toString() {
				return "2";
			}
		},
		/**
		 * 验证通过 - 3
		 */
		PASSED {
			@Override
			public String toString() {
				return "3";
			}
		},
		/**
		 * 信息不规范 - 0
		 */
		FAILED {
			@Override
			public String toString() {
				return "4";
			}
		}
	}
	
	public enum AgeFlag {
		/**
		 * 信息不规范 - 0
		 */
		INVALID {
			@Override
			public String toString() {
				return "0";
			}
		},
		/**
		 * 未成年 - 1
		 */
		NOT_ADULT {
			@Override
			public String toString() {
				return "1";
			}
		},
		/**
		 * 已成年 - 1
		 */
		ADULT {
			@Override
			public String toString() {
				return "2";
			}
		}
	}

	
	public enum Gender {
		/**
		 * 男 - 1
		 */
		MALE {
			@Override
			public String toString() {
				return "1";
			}
		},
		/**
		 * 女 - 0
		 */
		FEMALE {
			@Override
			public String toString() {
				return "0";
			}
		}
	}
	
	public enum State {
		/**
		 * 正常 - 1
		 */
		NORMAL {
			@Override
			public String toString() {
				return "1";
			}
		},
		/**
		 * 锁定 - 2
		 */
		LOCKED {
			@Override
			public String toString() {
				return "2";
			}
		},
		/**
		 * 转服中--3
		 */
		MOVEING {
			@Override
			public String toString() {
				return "3";
			}
		},
		/**
		 * 冻结(停封) - 9
		 */
		FORZEN {
			@Override
			public String toString() {
				return "9";
			}
		}
	}
	
	public enum Mobile {
		/**
		 * 未认证 0 
		 */
		NO_AUTH {
			@Override
			public String toString() {
				return "0";
			}
		},
		/**
		 * 已认证 1
		 */
		AUTHED {
			@Override
			public String toString() {
				return "1";
			}
		}
		
	}
	
	public enum Email {
		/**
		 * 未认证 0 
		 */
		NO_AUTH {
			@Override
			public String toString() {
				return "0";
			}
		},
		/**
		 * 已认证 1
		 */
		AUTHED {
			@Override
			public String toString() {
				return "1";
			}
		}
		
	}

	/**
	 * 账号ID - ID
	 */
	@Id
	@Column(name = "N_AID", nullable = false)
	private Long id;

	/**
	 * 帐号 - S_ACCOUNT
	 */
	@Column(name = "S_ACCOUNT", nullable = false, unique = true)
	private String account;

	/**
	 * 密码 - S_PASSWD
	 */
	@Column(name = "S_PASSWD", nullable = false)
	private String password;

	/**
	 * VIP级别 - S_VIP_LEVEL
	 */
	@Column(name = "S_VIP_LEVEL", nullable = false)
	private String vipLevel = VipLevel.NORMAL.toString();

	/**
	 * 真实姓名 - S_NAME
	 */
	@Column(name = "S_NAME")
	private String name;

	/**
	 * 身份证号码 - S_IDENTITY
	 */
	@Column(name = "S_IDENTITY")
	private String identity;

	/**
	 * 身份证验证状态 - S_ISPASS
	 */
	@Column(name = "S_ISPASS")
	private String identityAuthState = EpsStatus.NO_AUTH.toString();

	/**
	 * 年龄标志 - S_FLAG
	 */
	@Column(name = "S_FLAG")
	private String ageFlag;

	/**
	 * 性别 - S_GENDER
	 */
	@Column(name = "S_GENDER")
	private String gender;

	/**
	 * 身份证生日 - D_IDEN_BIRTH
	 */
	@Column(name = "D_IDEN_BIRTH")
	private Date identityBirthday;

	/**
	 * 生日 - D_BIRTH
	 */
	@Column(name = "D_BIRTH")
	private Date birthday;

	/**
	 * EMAIL - S_EMAIL
	 */
	@Column(name = "S_EMAIL", nullable = false)
	private String email;

	/**
	 * 帐号状态 - S_STATE
	 */
	@Column(name = "S_STATE", nullable = false)
	private String state = State.NORMAL.toString();

	/**
	 * 注册日期 - D_CREATE
	 */
	@Column(name = "D_CREATE", nullable = false)
	private Date createDate;

	/**
	 * 注册时IP - N_IP
	 */
	@Column(name = "N_IP")
	private Long regIp;

	/**
	 * 注册城市 - S_IP_CITY
	 */
	@Column(name = "S_IP_CITY")
	private Long regCityId;

	/**
	 * 区域运营商ID - N_ISSUER_ID
	 */
	@Column(name = "N_ISSUER_ID")
	private Long issuerId;

	/**
	 * 区域二级推广商ID - N_SPREADER_ID
	 */
	@Column(name = "N_SPREADER_ID")
	private Long spreaderId;
	
	/**
	 * EMAIL是否通过认证 - S_EMAIL_AUTHED
	 */
	@Column(name = "S_EMAIL_AUTHED")
	private String emailAuthed;

	/**
	 * 绑定手机 - S_MOBILE
	 */
	@Column(name = "S_MOBILE")
	private String mobile;
	
	/**
	 * 手机是否通过认证 - S_MOBILE_AUTHED
	 */
	@Column(name = "S_MOBILE_AUTHED")
	private String mobileAuthed;
	
	/**
	 * 用户别名
	 */
	@Column(name = "S_ALIASE")
	private String aliase;
	
	private String identifier;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAgeFlag() {
		return ageFlag;
	}

	public void setAgeFlag(String ageFlag) {
		this.ageFlag = ageFlag;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		if (identity != null) {
			this.identity = identity.toUpperCase();
		}
	}

	public String getIdentityAuthState() {
		return identityAuthState;
	}

	public void setIdentityAuthState(String identityAuthState) {
		this.identityAuthState = identityAuthState;
	}

	public Date getIdentityBirthday() {
		return identityBirthday;
	}

	public void setIdentityBirthday(Date identityBirthday) {
		this.identityBirthday = identityBirthday;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getRegIp() {
		return regIp;
	}

	public void setRegIp(Long regIp) {
		this.regIp = regIp;
	}

	public Long getSpreaderId() {
		return spreaderId;
	}

	public void setSpreaderId(Long spreaderId) {
		this.spreaderId = spreaderId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	
	public String getAliase() {
		return aliase;
	}

	public void setAliase(String aliase) {
		this.aliase = aliase;
	}
}
