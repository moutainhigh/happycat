package com.woniu.sncp.security.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(
		name="T_ACL_CREDENTIAL", 
		schema="SN_ACL"
)
public class CredentialEntity implements Serializable {

	private static final long serialVersionUID = -8635483953196804524L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 用户名
	 */
	@Column(name="S_USER_NAME", nullable=false, unique=true)
	private String userName;
	
	/**
	 * 密码
	 */
	@Column(name="S_PASSWORD", nullable=false)
	private String password;
	
	/**
	 * 帐号状态
	 * 0:激活状态 ACTIVED
	 * 1:冻结状态 FROZEN
	 */
	@Column(name="S_ENABLED", nullable=false)
	private String credentialState;
	
	/**
	 * 帐号创建时间
	 */
	@Column(name="D_CREATE_DATE", nullable=false)
	private Date createDate;
	
	/**
	 * 帐号更新时间
	 */
	@Column(name="D_UPDATE_DATE", nullable=false)
	private Date updateDate;
	
	/**
	 * 是否是超级用户
	 * 0:普通用户 NORMAL
	 * 1:超级用户 SUPER
	 */
	@Column(name="S_ISSYS", nullable=false)
	private String credentialType;
	
	/**
	 * 用户个人信息
	 */
	@OneToOne(mappedBy="credential", cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	private ProfileEntity profile;
	
	/**
	 * 合法应用服务器地址
	 */
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER, mappedBy="credential")
	private Set<ApplicationIPAddressEntity> ipAddresses = new HashSet<ApplicationIPAddressEntity>();
	
	/**
	 * 该帐号持有的角色组信息
	 */
	@ManyToMany(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinTable(name="T_ACL_GROUPS_USERS", schema="SN_ACL",
			inverseJoinColumns=@JoinColumn(name="S_GROUP_ID"),
			joinColumns=@JoinColumn(name="S_CREDENTIAL_ID"))
	private Set<GroupEntity> groupSet = new HashSet<GroupEntity>();
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCredentialState() {
		return credentialState;
	}

	public void setCredentialState(String credentialState) {
		this.credentialState = credentialState;
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

	public String getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(String credentialType) {
		this.credentialType = credentialType;
	}

	public ProfileEntity getProfile() {
		return profile;
	}

	public void setProfile(ProfileEntity profile) {
		this.profile = profile;
	}

	public Set<ApplicationIPAddressEntity> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(Set<ApplicationIPAddressEntity> ipAddresses) {
		for(ApplicationIPAddressEntity ip : ipAddresses) {
			ip.setCredential(this);
		}
		this.ipAddresses = ipAddresses;
	}
	
	public void addIpAddress(ApplicationIPAddressEntity ipAddress) {
		if(ipAddress != null) {
			ipAddresses.add(ipAddress);
			ipAddress.setCredential(this);
		}
	}
	
	public void removeIpAddress(ApplicationIPAddressEntity ipAddress) {
		if(ipAddress != null) {
			ipAddresses.remove(ipAddress);
			ipAddress.setCredential(null);
		}
	}

	public Set<GroupEntity> getGroupSet() {
		return groupSet;
	}

	public void setGroupSet(Set<GroupEntity> groupSet) {
		this.groupSet = groupSet;
	}
	
	public void addGroup(GroupEntity group) {
		if(group != null && !groupSet.contains(group)) {
			groupSet.add(group);
		}
	}
	
	public void removeGroup(GroupEntity group) {
		if(group != null && groupSet.contains(group)) {
			groupSet.remove(group);
		}
	}

}
