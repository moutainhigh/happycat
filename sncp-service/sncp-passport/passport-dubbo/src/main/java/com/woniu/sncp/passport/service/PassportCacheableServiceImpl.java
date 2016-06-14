package com.woniu.sncp.passport.service;

import org.dozer.DozerBeanMapper;
import org.perf4j.aop.Profiled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.woniu.snco.passport.entity.PassportEntity;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

@Service("passportCacheableService")
public class PassportCacheableServiceImpl implements PassportService {
	
	@Autowired
	@Qualifier("passportService")
	private PassportService passportService;
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	private RedisTemplate<String, PassportEntity> passportRedisTemplate;

	@Override
	@Profiled(tag = "PassportCacheableServiceImpl.findPassportByAccountOrAliase")
	public PassportDto findPassportByAccountOrAliase(String passportOrAliase)
			throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, SystemException {
		String key = redisTemplate.boundValueOps("PASSPORT:" + passportOrAliase.trim()).get();
		PassportEntity entity = getEntityFromRedis(key);
		if(entity != null) {
			DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
			PassportDto passportDto = dozerBeanMapper.map(entity, PassportDto.class);
			return passportDto;
		}
		return passportService.findPassportByAccountOrAliase(passportOrAliase);
	}
	
	@Override
	@Profiled(tag = "PassportCacheableServiceImpl.findPassportByAid")
	public PassportDto findPassportByAid(Long aid) throws PassportNotFoundException, SystemException {
		String key = redisTemplate.boundValueOps("AID:" + aid).get();
		PassportEntity entity = getEntityFromRedis(key);
		if(entity != null) {
			DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
			PassportDto passportDto = dozerBeanMapper.map(entity, PassportDto.class);
			return passportDto;
		}
		return passportService.findPassportByAid(aid);
	}

	private PassportEntity getEntityFromRedis(String key) {
		PassportEntity entity = null;
		if(StringUtils.hasText(key)) {
			entity = passportRedisTemplate.boundValueOps(key).get();
		}
		return entity;
	}

}
