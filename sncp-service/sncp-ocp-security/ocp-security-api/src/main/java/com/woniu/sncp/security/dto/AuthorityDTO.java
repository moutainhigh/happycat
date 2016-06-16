package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AuthorityDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	
	private String authorityName;
	
	private String authorityDescription;
	
	private String enabled;
	
	private Date createDate;
	
	private Date updateDate;
	
	private String resourceModule;
	
	private Set<RoleDTO> roles = new HashSet<RoleDTO>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthorityName() {
		return authorityName;
	}

	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}

	public String getAuthorityDescription() {
		return authorityDescription;
	}

	public void setAuthorityDescription(String authorityDescription) {
		this.authorityDescription = authorityDescription;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public Set<RoleDTO> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleDTO> roles) {
		this.roles = roles;
	}
	
	public void addRole(RoleDTO role) {
		if(role != null) {
			roles.add(role);
		}
	}
	
	public void removeRole(RoleDTO role) {
		if(role != null && roles.contains(role)) {
			roles.remove(role);
		}
	}

	public String getResourceModule() {
		return resourceModule;
	}

	public void setResourceModule(String resourceModule) {
		this.resourceModule = resourceModule;
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
		result = prime * result + ((authorityName == null) ? 0 : authorityName.hashCode());
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
		AuthorityDTO other = (AuthorityDTO) obj;
		if (authorityName == null) {
			if (other.authorityName != null)
				return false;
		} else if (!authorityName.equals(other.authorityName))
			return false;
		return true;
	}
	
}
