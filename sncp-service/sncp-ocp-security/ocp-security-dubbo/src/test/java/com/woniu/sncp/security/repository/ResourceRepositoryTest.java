package com.woniu.sncp.security.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.security.OcpSecurityApplication;
import com.woniu.sncp.security.entity.ResourceEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcpSecurityApplication.class)
public class ResourceRepositoryTest {
	
	@Autowired
	private ResourceRepository resourceRepository;

	@Test
	public void testFindByModuleAndEnbaled() {
		List<ResourceEntity> resourceEntities = resourceRepository.findByEnabled("0");
		assertNotNull(resourceEntities);
	}

}
