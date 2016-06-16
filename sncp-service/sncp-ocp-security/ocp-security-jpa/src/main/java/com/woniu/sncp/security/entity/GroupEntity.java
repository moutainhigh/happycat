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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		name="T_ACL_GROUP", 
		schema="SN_ACL",
		uniqueConstraints=@UniqueConstraint(columnNames = {"S_GROUP_NAME"}))
public class GroupEntity implements Serializable {

	private static final long serialVersionUID = -3905657051242389598L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 角色组名称
	 */
	@Column(name="S_GROUP_NAME", nullable=false)
	private String groupName;
	
	/**
	 * 角色组描述
	 */
	@Column(name="S_GROUP_DESCRIPTION")
	private String groupDescription;
	
	/**
	 * 该角色组是否启用
	 * 0:启用
	 * 1:禁用
	 */
	@Column(name="S_ENABLED", nullable=false)
	private String enabled;
	
	/**
	 * 创建时间
	 */
	@Column(name="D_CREATE_DATE", nullable=false)
	private Date createDate;
	
	/**
	 * 修改时间
	 */
	@Column(name="D_UPDATE_DATE", nullable=false)
	private Date updateDate;
	
	/**
	 * 角色组拥有的角色信息
	 */
	@ManyToMany(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinTable(name="T_ACL_GROUPS_ROLES", schema="SN_ACL",
			inverseJoinColumns=@JoinColumn(name="S_ROLE_ID"),
			joinColumns=@JoinColumn(name="S_GROUP_ID"))
	private Set<RoleEntity> roleSet = new HashSet<RoleEntity>();
	
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

	public Set<RoleEntity> getRoleSet() {
		return roleSet;
	}

	public void setRoleSet(Set<RoleEntity> roleSet) {
		this.roleSet = roleSet;
	}
	
	public void addRole(RoleEntity role) {
		if(role != null && !roleSet.contains(role)) {
			roleSet.add(role);
		}
	}
	
	public void removeRole(RoleEntity role) {
		if(role != null && roleSet.contains(role)) {
			roleSet.remove(role);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		GroupEntity other = (GroupEntity) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GroupEntity [id=" + id + ", groupName=" + groupName
				+ ", groupDescription=" + groupDescription + ", enabled="
				+ enabled + ", createDate=" + createDate + ", updateDate="
				+ updateDate + ", roleSet=" + roleSet + "]";
	}

}
