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
@Table(name="T_ACL_RESOURCE", schema="SN_ACL")
public class ResourceEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键
	 */
	@Id
	@Column(name="S_ID")
	private String id;
	
	/**
	 * 资源名称
	 */
	@Column(name="S_RESOURCE_NAME", nullable=false)
	private String resourceName;
	
	/**
	 * 资源类型
	 * 0:保护的资源为程序接口方法
	 * 1:保护的资源为URL
	 */
	@Column(name="S_RESOURCE_TYPE", nullable=false)
	private String resourceType;
	
	/**
	 * 要保护的URL,当resourceType为'1'时,该值才会被使用
	 */
	@Column(name="S_RESOURCE_URL")
	private String protectedUrl;
	
	/**
	 * 要保护的接口方法,当resourceType为'0'时,该值才会被使用
	 */
	@Column(name="S_RESOURCE_METHOD")
	private String protectedMethod;
	
	/**
	 * 资源描述
	 */
	@Column(name="S_RESOURCE_DESCRIPTION")
	private String resourceDescription;
	
	/**
	 * 该资源保护是否启用
	 * 0:启用
	 * 1:禁用
	 */
	@Column(name="S_ENABLED", nullable=false)
	private String enabled;
	
	@Column(name="S_MODULE", nullable=false)
	private String module;
	
	@ManyToMany(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinTable(name="T_ACL_AUTHORITIES_RESOURCES", schema="SN_ACL",
			inverseJoinColumns=@JoinColumn(name="S_AUTHORITY_ID"),
			joinColumns=@JoinColumn(name="S_RESOURCE_ID"))
	private Set<AuthorityEntity> authoritySet = new HashSet<AuthorityEntity>();
	
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getProtectedUrl() {
		return protectedUrl;
	}

	public void setProtectedUrl(String protectedUrl) {
		this.protectedUrl = protectedUrl;
	}

	public String getProtectedMethod() {
		return protectedMethod;
	}

	public void setProtectedMethod(String protectedMethod) {
		this.protectedMethod = protectedMethod;
	}

	public String getResourceDescription() {
		return resourceDescription;
	}

	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription = resourceDescription;
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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Set<AuthorityEntity> getAuthoritySet() {
		return authoritySet;
	}

	public void setAuthoritySet(Set<AuthorityEntity> authoritySet) {
		this.authoritySet = authoritySet;
	}

	@Override
	public String toString() {
		return "ResourceEntity [id=" + id + ", resourceName=" + resourceName + ", resourceType=" + resourceType
				+ ", protectedUrl=" + protectedUrl + ", protectedMethod=" + protectedMethod + ", resourceDescription="
				+ resourceDescription + ", enabled=" + enabled + ", module=" + module + ", authoritySet=" + authoritySet
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + "]";
	}
	
	
	
}
