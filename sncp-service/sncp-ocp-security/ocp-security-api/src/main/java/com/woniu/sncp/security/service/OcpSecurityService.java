package com.woniu.sncp.security.service;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.security.dto.CredentialDTO;
import com.woniu.sncp.security.dto.ResourceDTO;
import com.woniu.sncp.security.exception.CredentialNotFoundException;
import com.woniu.sncp.security.exception.ResourceNotFoundException;

/**
 * 接口安全相关服务
 * @author chenyx
 * @since JDK 1.8
 */
public interface OcpSecurityService {

	/**
	 * 根据用户名和密码查询身份信息
	 * @param username 用户名
	 * @param password 密码
	 * @return 身份信息
	 * @throws MissingParamsException 缺少参数异常
	 * @throws CredentialNotFoundException 身份信息未找到
	 * @throws SystemException 系统异常
	 */
	public CredentialDTO findCredntialByUsernameAndPassword(String username, String password) throws MissingParamsException, CredentialNotFoundException, SystemException;
	
	
	/**
	 * 根据模块名称查询资源
	 * @param module 模块名称
	 * @return 资源信息
	 * @throws MissingParamsException 缺少参数异常
	 * @throws ResourceNotFoundException 资源信息未找到
	 * @throws SystemException 系统异常
	 */
	public ResourceDTO findResourceByMatchUrl(String requestUrl) throws MissingParamsException, ResourceNotFoundException, SystemException;
	
	/**
	 * 重新加载配置信息
	 * @return
	 */
	public Boolean reload();
}
