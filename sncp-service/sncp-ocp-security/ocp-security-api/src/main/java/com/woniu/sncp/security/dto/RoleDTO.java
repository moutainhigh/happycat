package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RoleDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String roleName;
	
	private String roleDescription;
	
	private String enabled;
	
	private Date createDate;
	
	private Date updateDate;
	
	private Set<AuthorityDTO> authorities = new HashSet<AuthorityDTO>();
	
	public void addAuthority(AuthorityDTO authority) {
		if(authority != null) {
			authorities.add(authority);
		}
	}
	
	public void removeAuthority(AuthorityDTO authority) {
		if(authority != null && authorities.contains(authority)) {
			authorities.remove(authority);
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
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

	public Set<AuthorityDTO> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<AuthorityDTO> authorities) {
		this.authorities = authorities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
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
		RoleDTO other = (RoleDTO) obj;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		return true;
	}
	
}
