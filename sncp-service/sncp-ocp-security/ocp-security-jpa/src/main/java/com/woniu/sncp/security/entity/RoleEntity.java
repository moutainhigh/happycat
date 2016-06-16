package com.woniu.sncp.security.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		name="T_ACL_ROLE", 
		schema="SN_ACL",
		uniqueConstraints=@UniqueConstraint(columnNames = {"S_ROLE_NAME"}))
public class RoleEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 角色名称
	 */
	@Column(name="S_ROLE_NAME", nullable=false)
	private String roleName;
	
	/**
	 * 角色描述
	 */
	@Column(name="S_ROLE_DESCRIPTION")
	private String roleDescription;
	
	/**
	 * 角色是否启用
	 * 0:启用
	 * 1:禁用
	 */
	@Column(name="S_ENABLED", nullable=false)
	private String enabled;
	
	/**
	 * 创建时间
	 */
	@Column(name="D_CREATE_DATE")
	private Date createDate;
	
	/**
	 * 修改时间
	 */
	@Column(name="S_UPDATE_DATE")
	private Date updateDate;
	
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

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((roleName == null) ? 0 : roleName.hashCode());
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
		RoleEntity other = (RoleEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RoleEntity [id=" + id + ", roleName=" + roleName
				+ ", roleDescription=" + roleDescription + ", enabled="
				+ enabled + ", createDate=" + createDate + ", updateDate="
				+ updateDate + "]";
	}

}
