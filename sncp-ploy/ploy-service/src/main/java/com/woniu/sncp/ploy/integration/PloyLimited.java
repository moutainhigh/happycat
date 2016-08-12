package com.woniu.sncp.ploy.integration;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.Assert;
import org.springframework.transaction.annotation.Transactional;

import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.ploy.domain.PloyParticipator;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;
import com.woniu.sncp.ploy.repository.LargessPropsRepository;

/**
 * 活动限制
 * 
 * @author chenyx
 *
 */
@Component
public class PloyLimited {

	private static final Logger logger = LoggerFactory.getLogger(PloyLimited.class);

	@Autowired
	private LargessPropsRepository largessPropsRepository;

	/**
	 * 活动参数与次数限制
	 * 
	 * @param input
	 * @return
	 */
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Message<PloyTypeStatDTO> maxPloyLimited(Message<PloyTypeStatDTO> input) {
		String presentTimesLimit = StringUtils
				.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "maxPloy:", "|");
		// 限制赠送次数
		if (logger.isDebugEnabled()) {
			logger.debug("presentTimesLimit: " + presentTimesLimit);
		}
		if(StringUtils.isNotBlank(presentTimesLimit)) {
			PloyParticipator participator = (PloyParticipator) input.getHeaders().get("ployParticipatorFactory");
			PloyRequestDTO ployRequestDTO = participator.getPloyRequest();
			PassportDto passportDto = (PassportDto) input.getHeaders().get("Passport");
			Assert.isNull(passportDto, "passport is null");
			long haveGivenTimes = 0;
			if (ployRequestDTO.getGameAreaId() != null) {
				haveGivenTimes = largessPropsRepository
						.findCountUnionPloyBusinessLogByUserIdAndGameIdAndGameAreaIdAndPloyId(passportDto.getId(),
								ployRequestDTO.getGameId(), ployRequestDTO.getGameAreaId(),
								input.getPayload().getPresentsPloy().getId());
			} else {
				haveGivenTimes = largessPropsRepository.findCountUnionPloyBusinessLogByUserIdAndGameIdAndPloyId(
						passportDto.getId(), ployRequestDTO.getGameId(), input.getPayload().getPresentsPloy().getId());
			}
			//如果达到限制赠送次数，则清除所有赠送数据，并返回over状态
			if(haveGivenTimes >= Long.valueOf(presentTimesLimit).longValue()) {
				input.getPayload().clean();
			}
		}
		return input;
	}
}
