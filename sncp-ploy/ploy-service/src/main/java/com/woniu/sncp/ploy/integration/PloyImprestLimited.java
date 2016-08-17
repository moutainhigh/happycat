package com.woniu.sncp.ploy.integration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;
import com.woniu.sncp.imprest.service.ImprestService;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.ploy.domain.PloyParticipator;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;

/**
 * 活动充值限制
 * 
 * @author chenyx
 *
 */
public class PloyImprestLimited {

	private static final Logger logger = LoggerFactory.getLogger(PloyImprestLimited.class);

	@Autowired
	private ImprestService imprestService;

	/**
	 * 充值指定卡类型首充限制
	 * 
	 * @param input
	 * @return
	 */
	public Message<PloyTypeStatDTO> speCardsFirstLimited(Message<PloyTypeStatDTO> input) {
		String _speCardsFirst = StringUtils
				.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "specardsfirst:", "|");
		if (StringUtils.isNotBlank(_speCardsFirst)) {
			ImprestCardTypeDTO imprestCardTypeDTO = input.getHeaders().get("ImprestCardType", ImprestCardTypeDTO.class);
			Assert.notNull("ImprestCardType", "ImprestCardType is null ");
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
					if (CollectionUtils.isNotEmpty(speCards)
							&& !speCards.contains(imprestCardTypeDTO.getId().toString())) {
						logger.error(passportDto.getId() + ",指定充值返点活动:" + input.getPayload().getPresentsPloy().getId()
								+ ",不在指定的充值卡内:" + _speCardsFirst + ",本次充值卡:" + imprestCardTypeDTO.getId());
						input.getPayload().clean();
					}
				}
			}
		}
		return input;
	}

	/**
	 * 最多返点值限制
	 * 
	 * @param input
	 * @return
	 */
	public Message<PloyTypeStatDTO> returnMaxPointLimited(Message<PloyTypeStatDTO> input) {
		String returnMaxPoint = StringUtils
				.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "ReturnMaxPoint:", "|");
		logger.debug("充值累计返点活动，本次返点最大值为 :" + returnMaxPoint);
		if (StringUtils.isNotBlank(returnMaxPoint)) {
			PassportDto passportDto = input.getHeaders().get("Passport", PassportDto.class);
			Assert.isNull(passportDto, "passport is null");
			ImprestLogDTO imprestLog = input.getHeaders().get("ImprestLog", ImprestLogDTO.class);
			Assert.isNull(imprestLog, "imprestLog is null");
			Date start = DateUtils.truncate(imprestLog.getImprestDate(), Calendar.DATE);
			Date end = DateUtils.truncate(DateUtils.addDays(imprestLog.getImprestDate(), 1), Calendar.DATE);
			// 查询用户返点记录
			BigDecimal returnAmount = imprestService.findSumLargessPoints(passportDto.getId(), start, end,
					input.getPayload().getPresentsPloy().getType(), null);
			if (returnAmount == null || returnAmount.longValue() >= Long.valueOf(returnMaxPoint).longValue()) {
				logger.debug("充值累计返点活动 最高返回，不再赠送.....");
				logger.debug("passportId: " + passportDto.getId() + ",已返点记录: " + returnAmount + ",本次返点最大值: "
						+ returnMaxPoint);
				input.getPayload().clean();
			}
		}
		return input;
	}

	/**
	 * 充值总额限制条件，必须大于充值总额
	 * 
	 * @param input
	 * @return
	 */
	public Message<PloyTypeStatDTO> imprestAmountLimited(Message<PloyTypeStatDTO> input) {
		String imprestAmount = StringUtils
				.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|", "imprestAmount:", "|");
		if (StringUtils.isNotBlank(imprestAmount)) {
			PloyParticipator participator = (PloyParticipator) input.getHeaders().get("ployParticipatorFactory");
			PloyRequestDTO ployRequestDTO = participator.getPloyRequest();
			PassportDto passportDto = input.getHeaders().get("Passport", PassportDto.class);
			Assert.isNull(passportDto, "passport is null");
			ImprestLogDTO imprestLog = input.getHeaders().get("ImprestLog", ImprestLogDTO.class);
			Assert.isNull(imprestLog, "imprestLog is null");
			Date start = DateUtils.truncate(imprestLog.getImprestDate(), Calendar.DATE);
			Date end = DateUtils.truncate(DateUtils.addDays(imprestLog.getImprestDate(), 1), Calendar.DATE);
			BigDecimal _imprestAmount = imprestService.findSumImprestAmount(Long.valueOf(ployRequestDTO.getGameId()),
					passportDto.getId(), start, end);
			if (_imprestAmount == null || _imprestAmount.longValue() < Long.valueOf(imprestAmount).longValue()) {
				input.getPayload().clean();
			}
		}
		return input;
	}

	public Message<PloyTypeStatDTO> imprestMinAmountLimited(Message<PloyTypeStatDTO> input) {
		String _impmoney = StringUtils.substringBetween(input.getPayload().getPresentsPloy().getLimitContent() + "|",
				"min:", "|");
		if (StringUtils.isNotBlank(_impmoney)) {
			PloyParticipator participator = (PloyParticipator) input.getHeaders().get("ployParticipatorFactory");
			PloyRequestDTO ployRequestDTO = participator.getPloyRequest();
			PassportDto passportDto = input.getHeaders().get("Passport", PassportDto.class);
			Assert.isNull(passportDto, "passport is null");
			BigDecimal impMoney = imprestService.findSumImprestAmount(passportDto.getId(), ployRequestDTO.getGameId(),
					ployRequestDTO.getGameAreaId(), "A", input.getPayload().getPresentsPloy().getStart(), input.getPayload().getPresentsPloy().getEnd());
			logger.debug("充值配送金额未设置或者设置不正确," + input.getPayload().getPresentsPloy().getType());
			 
			return null;
		}

		logger.debug("充值配送活动累计个人充值 : " + impMoney + " 活动配置 最低金额: " + _impmoney);
	}

}
