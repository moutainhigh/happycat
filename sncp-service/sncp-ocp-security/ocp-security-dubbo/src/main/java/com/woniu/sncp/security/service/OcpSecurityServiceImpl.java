package com.woniu.sncp.security.service;

import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.security.dto.CredentialDTO;
import com.woniu.sncp.security.dto.ResourceDTO;
import com.woniu.sncp.security.entity.CredentialEntity;
import com.woniu.sncp.security.entity.ResourceEntity;
import com.woniu.sncp.security.exception.CredentialNotFoundException;
import com.woniu.sncp.security.exception.ResourceNotFoundException;
import com.woniu.sncp.security.repository.CredentialRepository;
import com.woniu.sncp.security.repository.ResourceRepository;

@SuppressWarnings("deprecation")
public class OcpSecurityServiceImpl implements OcpSecurityService, InitializingBean {
	
	private static final String CREDENTIAL_ENABLED_STATE = "0";
	
	private static final String RESOURCE_ENABLED_STATE = "0";
	
	private List<CredentialEntity> credentialList;
	
	private List<ResourceEntity> resourceList;
	
	@Autowired
	private CredentialRepository credentialRepository;
	
	@Autowired
	private ResourceRepository resourceRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public CredentialDTO findCredntialByUsernameAndPassword(String username, String password)
			throws MissingParamsException, CredentialNotFoundException, SystemException {
		Assert.notNull(credentialList, "credentialList is null");
		if(!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			throw new MissingParamsException("username and password are required");
		}
		String endocderPassword = passwordEncoder.encodePassword(password.trim(), username.trim());
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		CredentialDTO credentialDTO = null;
		for(CredentialEntity entity : credentialList) {
			if(entity.getUserName().equalsIgnoreCase(username.trim())) {
				credentialDTO = new CredentialDTO();
				beanMapper.map(entity, credentialDTO);
			}
		}
		if(credentialDTO == null || !credentialDTO.getPassword().equals(endocderPassword)) {
			throw new CredentialNotFoundException(new String[]{"Credential not found"});
		}
		return credentialDTO;
	}

	@Override
	public ResourceDTO findResourceByMatchUrl(String requestUrl)
			throws MissingParamsException, ResourceNotFoundException, SystemException {
		Assert.notNull(resourceList, "resourceList is null");
		if(!StringUtils.hasText(requestUrl)) {
			throw new MissingParamsException("requestUrl is required");
		}
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		ResourceDTO resourceDTO = null;
		for(ResourceEntity entity : resourceList) {
			AntPathMatcher antPathMatcher = new AntPathMatcher();
			if(StringUtils.hasText(entity.getProtectedUrl())) {
				if(antPathMatcher.match(entity.getProtectedUrl(), requestUrl)) {
					resourceDTO = new ResourceDTO();
					beanMapper.map(entity, resourceDTO);
				}
			}
		}
		if(resourceDTO == null) {
			throw new ResourceNotFoundException(new String[]{"resource not found"});
		}
		return resourceDTO;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		reload();
	}

	@Override
	public Boolean reload() {
		credentialList = credentialRepository.findByCredentialState(CREDENTIAL_ENABLED_STATE);
		resourceList = resourceRepository.findByEnabled(RESOURCE_ENABLED_STATE);
		return true;
	}

}
