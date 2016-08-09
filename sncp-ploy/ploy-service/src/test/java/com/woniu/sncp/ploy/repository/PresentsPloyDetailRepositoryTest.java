package com.woniu.sncp.ploy.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.entity.PresentsPloyDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class PresentsPloyDetailRepositoryTest {
	
	@Autowired
	private PresentsPloyDetailRepository presentsPloyDetailRepository;

	@Test
	public void testGetOne() {
		PresentsPloyDetail ployDetail = presentsPloyDetailRepository.findOne(4946L);
		assertNotNull(ployDetail);
	}
	
	@Test
	public void testFindByPloyId() {
		List<PresentsPloyDetail> passportPresentsPloyDetails = presentsPloyDetailRepository.findByPloyId(782L);
		assertNotNull(passportPresentsPloyDetails);
	}

}
