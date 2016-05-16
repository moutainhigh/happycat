package com.woniu.sncp.passport.service;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.passport.PassportApplication;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PassportApplication.class)
public class PassportServiceImplTest {
	
	@Autowired
	private PassportService passportService;
	
	@Autowired
    private LoadBalancerClient loadBalancer;

	@Test
	public void testFindPassportByAccountOrAliase() {
		PassportDto passportDto = null;
		try {
			passportDto = passportService.findPassportByAccountOrAliase("test123");
		} catch (PassportNotFoundException | PassportHasFrozenException | PassportHasLockedException e) {
			e.printStackTrace();
		}
		assertNotNull(passportDto);
	}
	
	@Test
	public void testFindPassportByAid() {
		PassportDto passportDto = null;
		try {
			passportDto = passportService.findPassportByAid(123456L);
		} catch (PassportNotFoundException e) {
			e.printStackTrace();
		}
		assertNotNull(passportDto);
	}
	
	@Test
	public void doStuff() {
        ServiceInstance instance = loadBalancer.choose("passports");
        URI storesUri = URI.create(String.format("http://%s:%s", instance.getHost(), instance.getPort()));
        System.out.println(storesUri);
    }

}
