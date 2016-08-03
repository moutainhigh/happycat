package com.woniu.sncp.passport.service;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.passport.PassportApplication;
import com.woniu.sncp.passport.dto.OcpResponsePassportDto;
import com.woniu.sncp.passport.dto.PassportDto;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PassportApplication.class)
public class GatewayClientTest {
	
	@Autowired
	private OcpPassportGatewayClient gatewayClient;

	/*@Test
	public void testGetSessionId() {
		String params = "id=12345";
		PassportDto dto = new PassportDto();
		dto.setId(12345L);
		OcpResponsePassportDto result = gatewayClient.getSessionId(params);
		System.out.println(result);
	}*/

}
