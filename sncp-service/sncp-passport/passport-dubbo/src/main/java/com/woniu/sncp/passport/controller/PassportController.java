package com.woniu.sncp.passport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;
import com.woniu.sncp.passport.service.PassportService;

@RestController
public class PassportController {
	
	@Autowired
	private PassportService passportService;
	
	@RequestMapping("/passport/{passport}")
	public PassportDto findPassport(@PathVariable("passport") String passport) throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, SystemException {
		return passportService.findPassportByAccountOrAliase(passport);
	}
	
	@RequestMapping("/passport/id/{id}")
	public PassportDto findPassportById(@PathVariable("id") Long id) {
		return passportService.findIsFreeCardUser(id);
	}
}
