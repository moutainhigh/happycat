package com.woniu.sncp.ploy.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.ploy.PloyApplication;
import com.woniu.sncp.ploy.entity.LargessProps;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PloyApplication.class)
public class LargessPropsRepositoryTest {
	
	@Autowired
	private LargessPropsRepository largessPropsRepository;

	@Test
	public void testFindOne() {
		LargessProps largessProps = largessPropsRepository.findOne(21575427L);
		assertNotNull(largessProps);
	}
	
	@Test
	public void testFindCountUnionPloyBusinessLogByUserIdAndGameIdAndPloyId() {
		int result = largessPropsRepository.findCountUnionPloyBusinessLogByUserIdAndGameIdAndGameAreaIdAndPloyId(1503470095L, 17L, 7170007L, 4544L);
		System.out.println(result);
	}

}
