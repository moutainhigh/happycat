package com.woniu.sncp.ploy.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.entity.PassportPresentsPloyDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PassportPresentsPloyDetailRepositoryTest {
	
	@Autowired
	private PassportPresentsPloyDetailRepository passportPresentsPloyDetailRepository;

	@Test
	public void testGetOne() {
		PassportPresentsPloyDetail ployDetail = passportPresentsPloyDetailRepository.findOne(4946L);
		assertNotNull(ployDetail);
	}
	
	@Test
	public void testFindByPloyId() {
		List<PassportPresentsPloyDetail> passportPresentsPloyDetails = passportPresentsPloyDetailRepository.findByPloyId(782L);
		assertNotNull(passportPresentsPloyDetails);
	}

}
