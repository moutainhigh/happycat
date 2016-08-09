package com.woniu.sncp.ploy.integration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;

/**
 * 活动充值限制
 * @author chenyx
 *
 */
public class PloyImprestLimited {
	
	private static final Logger logger = LoggerFactory.getLogger(PloyImprestLimited.class);

	
	/**
	 * 充值指定卡类型首充限制
	 * @param input
	 * @return
	 */
	public Message<PloyTypeStatDTO>  speCardsFirstLimited(Message<PloyTypeStatDTO> input) {
		String _speCardsFirst = StringUtils.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "specardsfirst:", "|");
		if(StringUtils.isNotBlank(_speCardsFirst)) {
			ImprestCardTypeDTO imprestCardTypeDTO = input.getHeaders().get("ImprestCardType",ImprestCardTypeDTO.class);
			Assert.notNull("ImprestCardType","ImprestCardType is null ");
			PassportDto passportDto = input.getHeaders().get("Passport", PassportDto.class);
			Assert.isNull(passportDto, "passport is null");
			String[] _speCards = null;
			List<Long> speCards = new ArrayList<Long>();
			if (StringUtils.isNotBlank(_speCardsFirst)) {
				_speCards = StringUtils.split(_speCardsFirst, ",");
				if (!ArrayUtils.isEmpty(_speCards)) {
					for (String _speCard : _speCards) {
						long speCard = NumberUtils.toLong(_speCard);
						if (speCard != 0)
							speCards.add(speCard);
					}
					if (CollectionUtils.isNotEmpty(speCards) && !speCards.contains(imprestCardTypeDTO.getId().toString())) {
						logger.error(passportDto.getId() + ",指定充值返点活动:" + input.getPayload().getPresentsPloy().getId() + 
								",不在指定的充值卡内:" + _speCardsFirst + ",本次充值卡:" + imprestCardTypeDTO.getId());
						input.getPayload().clean();
					}
				}
			}
		}
		return input;
	}
}
