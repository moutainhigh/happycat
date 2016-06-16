package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CredentialDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String userName;
	
	private String password;
	
	private String credentialState;
	
	private Date createDate;
	
	private Date updateDate;
	
	private String credentialType;
	
	private ProfileDTO profile;
	
	private Set<AppIPDTO> ipAddresses = new HashSet<AppIPDTO>();
	
	private Set<GroupDTO> groupSet = new HashSet<GroupDTO>();
	
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

	public String getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(String credentialType) {
		this.credentialType = credentialType;
	}

	public ProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ProfileDTO profile) {
		if(profile != null) {
			this.profile = profile;
			profile.setCredential(this);
		}
	}
	
	public Set<AppIPDTO> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(Set<AppIPDTO> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public Set<GroupDTO> getGroupSet() {
		return groupSet;
	}

	public void setGroupSet(Set<GroupDTO> groupSet) {
		this.groupSet = groupSet;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CredentialDTO other = (CredentialDTO) obj;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	
	
}
