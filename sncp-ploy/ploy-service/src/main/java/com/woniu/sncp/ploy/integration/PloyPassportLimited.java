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
import com.woniu.sncp.ploy.domain.PloyParticipator;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;
import com.woniu.sncp.vip.dto.PassportVipDTO;
import com.woniu.sncp.vip.service.PassportVipService;

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
	
	@Autowired
	private PassportVipService passportVipService;

	/**
	 * 蜗牛移动号码限制
	 * @param input
	 * @return
	 */
	@Router
	public Message<PloyTypeStatDTO> snailLimited(Message<PloyTypeStatDTO> input) {
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
	
	/**
	 * vip限制
	 * @param input
	 * @return
	 */
	public Message<PloyTypeStatDTO> vipLimited(Message<PloyTypeStatDTO> input) {
		PloyParticipator participator = (PloyParticipator) input.getHeaders().get("ployParticipatorFactory");
		PloyRequestDTO ployRequestDTO = participator.getPloyRequest();
		PassportDto passportDto = (PassportDto) input.getHeaders().get("Passport");
		Assert.isNull(passportDto, "passport is null");
		PassportVipDTO passportVipDTO = passportVipService.findPassportVipByAidAndGameId(passportDto.getId(), ployRequestDTO.getGameId());
		String vipLimited = StringUtils.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "VIP:", "|");
		if(StringUtils.isNotBlank(vipLimited)) {
			int vipLimitedInt = Integer.valueOf(vipLimited);
			if(passportVipDTO == null || Integer.valueOf(passportVipDTO.getVipLevel()).intValue() <  vipLimitedInt) {
				input.getPayload().clean();
			}
		}
		return input;
	}
}
