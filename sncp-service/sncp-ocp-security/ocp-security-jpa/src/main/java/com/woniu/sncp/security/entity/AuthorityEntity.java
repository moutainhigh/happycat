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

@Entity
@Table(name="T_ACL_AUTHORITY", schema="SN_ACL")
public class AuthorityEntity implements Serializable {

	private static final long serialVersionUID = 1785589474832640815L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 权限名称
	 */
	@Column(name="S_AUTHORITY_NAME", nullable=false, unique=true)
	private String authorityName;
	
	/**
	 * 权限描述
	 */
	@Column(name="S_AUTHORITY_DESCRIPTION")
	private String authorityDescription;
	
	/**
	 * 该权限是否启用
	 * 0:启用
	 * 1:禁用
	 */
	@Column(name="S_ENABLED", nullable=false)
	private String enabled;
	
	/**
	 * 创建日期
	 */
	@Column(name="D_CREATE_DATE", nullable=false)
	private Date createDate;
	
	/**
	 * 修改日期
	 */
	@Column(name="D_UPDATE_DATE", nullable=false)
	private Date updateDate;
	
	/**
	 * 权限拥有的资源信息
	 */
	@ManyToMany(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinTable(name="T_ACL_ROLES_AUTHORITIES", schema="SN_ACL",
			inverseJoinColumns=@JoinColumn(name="S_ROLE_ID"),
			joinColumns=@JoinColumn(name="S_AUTHORITY_ID"))
	private Set<RoleEntity> roleSet = new HashSet<RoleEntity>();
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((authorityName == null) ? 0 : authorityName.hashCode());
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
		AuthorityEntity other = (AuthorityEntity) obj;
		if (authorityName == null) {
			if (other.authorityName != null)
				return false;
		} else if (!authorityName.equals(other.authorityName))
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
		return "AuthorityEntity [id=" + id + ", authorityName=" + authorityName
				+ ", authorityDescription=" + authorityDescription
				+ ", enabled=" + enabled + ", createDate=" + createDate
				+ ", updateDate=" + updateDate + ", resourceSet=" + roleSet
				+ "]";
	}
	
}
