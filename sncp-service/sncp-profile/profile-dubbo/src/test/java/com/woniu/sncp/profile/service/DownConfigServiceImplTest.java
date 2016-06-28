package com.woniu.sncp.profile.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.profile.ProfileApplication;
import com.woniu.sncp.profile.dto.PaginationTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ProfileApplication.class)
public class DownConfigServiceImplTest {
	@Autowired DownConfigServiceImpl service;
	
	public @Test void testQuery(){
		PaginationTo query = service.query("1", "1", 2, 1);
		Assert.assertNotNull(query);
	}
}
