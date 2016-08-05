package com.woniu.sncp.ploy.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;
import com.woniu.sncp.imprest.dto.ImprestOrderDTO;
import com.woniu.sncp.imprest.service.ImprestService;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.passport.service.PassportService;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;

@Component
public class PloyHeaderEnricher {

	@Autowired
	private PassportService passportService;

	@Autowired
	private ImprestService imprestService;

	public PassportDto findPassport(PloyRequestDTO ployRequest) {
		PassportDto passportDto = null;
		if (ployRequest.getAid() != null) {
			try {
				passportDto = passportService.findPassportByAid(ployRequest.getAid());
			} catch (PassportNotFoundException | SystemException e) {
				e.printStackTrace();
			}
		}
		return passportDto;
	}

	public ImprestLogDTO findImprestLog(PloyRequestDTO ployRequest) {
		ImprestLogDTO imprestLogDTO = null;
		if (ployRequest.getImprestLogId() != null) {
			imprestLogDTO = imprestService.findImprestLogById(Long.valueOf(ployRequest.getImprestLogId()));
		}
		return imprestLogDTO;
	}

	public ImprestCardTypeDTO findImprestCardType(PloyRequestDTO ployRequest) {
		ImprestCardTypeDTO imprestCardTypeDTO = null;
		if (ployRequest.getCardTypeId() != null) {
			imprestCardTypeDTO = imprestService.findImprestCardById(ployRequest.getCardTypeId());
		}
		return imprestCardTypeDTO;
	}

	public ImprestOrderDTO findImprestOrder(PloyRequestDTO ployRequest) {
		ImprestOrderDTO imprestOrderDTO = null;
		if (StringUtils.hasText(ployRequest.getImpOrderNo()) && ployRequest.getGameAreaId() != null
				&& ployRequest.getAid() != null) {
			imprestOrderDTO = imprestService.findImprestOrderByOrderNoAndGameAreaIdAndAid(ployRequest.getImpOrderNo(),
					ployRequest.getGameAreaId(), ployRequest.getAid());
		}
		return imprestOrderDTO;
	}

}
