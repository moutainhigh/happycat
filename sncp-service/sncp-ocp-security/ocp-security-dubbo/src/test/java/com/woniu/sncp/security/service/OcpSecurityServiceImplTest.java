package com.woniu.sncp.security.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.security.OcpSecurityApplication;
import com.woniu.sncp.security.dto.CredentialDTO;
import com.woniu.sncp.security.dto.ResourceDTO;
import com.woniu.sncp.security.exception.CredentialNotFoundException;
import com.woniu.sncp.security.exception.ResourceNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcpSecurityApplication.class)
public class OcpSecurityServiceImplTest {
	
	@Autowired
	private OcpSecurityService ocpSecurityService;

	@Test
	public void testFindCredntialByUsernameAndPassword() throws MissingParamsException, CredentialNotFoundException, SystemException {
		CredentialDTO credentialDTO = ocpSecurityService.findCredntialByUsernameAndPassword("test_app", "111111");
		assertEquals("test_app", credentialDTO.getUserName());
	}

	@Test
	public void testFindResourceByMatchUrl() throws MissingParamsException, ResourceNotFoundException, SystemException {
		ResourceDTO resourceDTO = ocpSecurityService.findResourceByMatchUrl("/passport/bindmobile/aaa");
		assertEquals("Passport Email Binding", resourceDTO.getResourceName());
	}

}
