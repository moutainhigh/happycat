package com.woniu.sncp.security.repository;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.security.OcpSecurityApplication;
import com.woniu.sncp.security.entity.CredentialEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcpSecurityApplication.class)
public class CredentialRepositoryTest<E> {
	
	@Autowired
	private CredentialRepository credentialRepository;

	@Test
	public void testFindByUserName() throws UnsupportedEncodingException {
		Md5PasswordEncoder encoder = new Md5PasswordEncoder();
		String password = encoder.encodePassword("111111", "test_app");
		//String password = new String(DigestUtils.md5Digest("111111".getBytes()));
		CredentialEntity entity = credentialRepository.findByUserName("test_app",password);
		//List<CredentialEntity> credentialEntities = credentialRepository.findAll();
		assertNotNull(entity);
	}

}
