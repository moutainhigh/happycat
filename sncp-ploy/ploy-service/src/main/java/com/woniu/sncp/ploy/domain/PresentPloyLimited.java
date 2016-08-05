package com.woniu.sncp.ploy.domain;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;
import com.woniu.sncp.ploy.entity.PresentsPloy;

/**
 * 活动限制
 * @author chenyx
 *
 */
public class PresentPloyLimited {
	
	private static final Logger logger = LoggerFactory.getLogger(PresentPloyLimited.class);

	public PloyTypeStatDTO snailLimited(PloyTypeStatDTO ployTypeStatDTO) {
		PresentsPloyDTO presentsPloy = ployTypeStatDTO.getPresentsPloy();
		String snailLimit = StringUtils.substringBetween(presentsPloy.getLimitContent() + "|", "limitSnail:", "|");
		if(logger.isDebugEnabled()) {
			logger.debug("snailLimit: " + snailLimit);
		}
	/*	if (StringUtils.isNotBlank(snailLimit) && "1".equals(snailLimit) && !isFreeCard(pp.getAliase())){
			logger.debug("aliase is not 170, aliase is: " + pp.getAliase());
			ployTypeStatDTO.setState(PloyTypeStatDTO.State.OVER);
		} else {
			ployTypeStatDTO.setState(PloyTypeStatDTO.State.NEXT);
		}*/
		return null;
	}
}
