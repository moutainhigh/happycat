package com.woniu.sncp.security.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.security.OcpSecurityApplication;
import com.woniu.sncp.security.entity.AuthorityEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcpSecurityApplication.class)
public class AuthorityRepositoryTest {

	@Autowired
	private AuthorityRepository authorityRepository;

	@Test
	public void testFindByAuthorityName() {
		AuthorityEntity entity = authorityRepository.findByAuthorityName("testzj");
		assertNotNull(entity);
	}

}
