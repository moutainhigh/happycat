package com.woniu.sncp.ploy.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Router;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.service.PassportService;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;

/**
 * 活动帐号限制
 * @author chenyx
 *
 */
@Component
public class PloyPassportLimited {
	
	private static final Logger logger = LoggerFactory.getLogger(PloyLimited.class);
	
	@Autowired
	private PassportService passportService;

	/**
	 * 蜗牛移动号码限制
	 * @param input
	 * @return
	 */
	@Router
	public Message<PloyTypeStatDTO> limitSnail(Message<PloyTypeStatDTO> input) {
		String snailLimit = StringUtils.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "limitSnail:", "|");
		if(logger.isDebugEnabled()) {
			logger.debug("snailLimit: " + snailLimit);
		}
		if(StringUtils.isNotBlank(snailLimit)) {
			PassportDto passportDto = (PassportDto) input.getHeaders().get("Passport");
			Assert.isNull(passportDto, "passport is null");
			PassportDto resultPassport = passportService.findIsFreeCardUser(passportDto.getId());
			if(resultPassport == null) {
				input.getPayload().clean();
			}
		}
		return input;
	}
}
