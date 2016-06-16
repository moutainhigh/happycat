package com.woniu.sncp.security.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.security.OcpSecurityApplication;
import com.woniu.sncp.security.entity.RoleEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcpSecurityApplication.class)
public class RoleRepositoryTest {
	
	@Autowired
	private RoleRepository roleRepository;

	/*@Test
	public void testFindByReouseceModule() {
		List<RoleEntity> roleEnties = roleRepository.findByReouseceModule("ocp-user-service");
		for(RoleEntity e : roleEnties) {
			System.out.println(e);
		}
		assertNotNull(roleEnties);
	}*/
	
	@Test
	public void testIPRange() {
		String pattern = "192.168.95.*";
		String ip = "192.168.95.121";
		SubnetUtils subnetUtils = new SubnetUtils(pattern);
		System.out.println(subnetUtils.getInfo().isInRange(ip));
	}

}
