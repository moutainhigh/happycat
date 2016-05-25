package com.woniu.sncp.alarm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.exception.BusinessException;
import com.woniu.sncp.nciic.NciicApplication;
import com.woniu.sncp.nciic.dto.NciicMessageOut;
import com.woniu.sncp.nciic.dto.NciicMessageIn;
import com.woniu.sncp.nciic.service.NciicMessageService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(NciicApplication.class)
public class NciicMessageServiceImplTest {

	@Autowired
	NciicMessageService nciicMessageService;

	@Test
	public void testNciicMessageService()
			throws BusinessException {
		NciicMessageOut out = nciicMessageService.checkRealNameIdentityNo(new NciicMessageIn("毛从长", "320504198301242753"));
		Assert.assertNotNull(out.getErrorInfo());
		Assert.assertEquals("身份证校验结果", NciicMessageOut.SUCC_SAME, out.getIdentityNoResult());
		Assert.assertEquals("姓名校验结果", NciicMessageOut.SUCC_SAME, out.getUserNameResult());
	}

}
