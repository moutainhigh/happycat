package com.woniu.sncp.passport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.passport.service.PassportService;

@RestController
public class PassportController {

	@Autowired
	@Qualifier("passportCacheableService")
	private PassportService passportService;

	@RequestMapping("/passport/{aid}")
	public PassportDto findByAid(@PathVariable("aid") String aid)
			throws NumberFormatException, PassportNotFoundException, SystemException {
		return passportService.findPassportByAid(Long.valueOf(aid));
	}

	/*@RequestMapping("/passport/{aid}")
	public String findByAid(@PathVariable("aid") String aid) {
		String result = redisTemplate.boundValueOps("AID:" + aid).get();
		return redisTemplate.boundValueOps(result).get();
	}*/
}
