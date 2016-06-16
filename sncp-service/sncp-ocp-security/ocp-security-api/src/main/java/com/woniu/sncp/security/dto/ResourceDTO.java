package com.woniu.sncp.security.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ResourceDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String resourceName;
	
	private String resourceType;
	
	private String protectedUrl;
	
	private String protectedMethod;
	
	private String resourceDescription;
	
	private String enabled;
	
	private Date createDate;
	
	private Date updateDate;
	
	private String module;
	
	private Set<AuthorityDTO> authorities = new HashSet<AuthorityDTO>();
	
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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
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
	
}
