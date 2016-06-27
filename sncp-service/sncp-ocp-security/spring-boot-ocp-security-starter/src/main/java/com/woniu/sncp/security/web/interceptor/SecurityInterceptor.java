package com.woniu.sncp.security.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.woniu.sncp.security.dto.AppIPDTO;
import com.woniu.sncp.security.dto.CredentialDTO;
import com.woniu.sncp.security.exception.IpAddressIsNotValidException;
import com.woniu.sncp.security.service.OcpSecurityService;

public class SecurityInterceptor extends HandlerInterceptorAdapter {
	
	private OcpSecurityService ocpSecurityService;
	
	private String requestIpHeader;
	
	private static final String Header_OCP_APP_ID = "H_APPID";
	
	private static final String Header_OCP_APP_PWD = "H_PWD"; 

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestUrl = request.getRequestURI();
		String username = request.getHeader(Header_OCP_APP_ID);
		String password = request.getHeader(Header_OCP_APP_PWD);
		
		CredentialDTO credentialDTO = ocpSecurityService.findCredntialByUsernameAndPassword(username, password);
		
		String ip = getIPAddress(request);
		if(!credentialDTO.getIpAddresses().contains(new AppIPDTO(ip))) {
			throw new IpAddressIsNotValidException(new String[]{username,ip});
		}
		
		ocpSecurityService.findResourceByMatchUrl(requestUrl);
		
		return true;
	}
	
	private String getIPAddress(HttpServletRequest request) {
		String ip = null;
		if(StringUtils.hasText(requestIpHeader)) {
			ip = request.getHeader("requestIpHeader");
		}
		if(StringUtils.hasText(ip)) {
			return ip;
		} else {
			return request.getRemoteAddr();
		}
	}

	public OcpSecurityService getOcpSecurityService() {
		return ocpSecurityService;
	}

	public void setOcpSecurityService(OcpSecurityService ocpSecurityService) {
		this.ocpSecurityService = ocpSecurityService;
	}

	public String getRequestIpHeader() {
		return requestIpHeader;
	}

	public void setRequestIpHeader(String requestIpHeader) {
		this.requestIpHeader = requestIpHeader;
	}
	
	
	
}
