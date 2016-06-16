package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class GroupDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String groupName;
	
	private String groupDescription;
	
	private String enabled;
	
	private Date createDate;
	
	private Date updateDate;
	
	private Set<RoleDTO> roleSet = new HashSet<RoleDTO>(); 

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	
	public Set<RoleDTO> getRoleSet() {
		return roleSet;
	}

	public void setRoleSet(Set<RoleDTO> roleSet) {
		this.roleSet = roleSet;
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
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
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
		GroupDTO other = (GroupDTO) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		return true;
	}
	
}
