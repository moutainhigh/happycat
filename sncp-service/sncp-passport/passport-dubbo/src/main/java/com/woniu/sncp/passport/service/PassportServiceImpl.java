package com.woniu.sncp.passport.service;

import java.util.Calendar;

import org.dozer.DozerBeanMapper;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.passport.config.OcpPassportProfile;
import com.woniu.sncp.passport.dto.OcpResponseDto;
import com.woniu.sncp.passport.dto.OcpResponsePassportDto;
import com.woniu.sncp.passport.dto.PassportDto;
import com.woniu.sncp.passport.exception.PassportHasFrozenException;
import com.woniu.sncp.passport.exception.PassportHasLockedException;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

/**
 * 帐号相关接口实现
 * 
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
@Service("passportService")
public class PassportServiceImpl implements PassportService {

	private Logger logger = LoggerFactory.getLogger(PassportServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OcpPassportGatewayClient ocpPassportGatewayClient;

	@Autowired
	private OcpPassportProfile ocpPassportProfile;

	@Autowired
	private HttpHeaders httpHeaders;

	private static final String ERROR_STATE = "1";

	/**
	 * 服务地址
	 */
	@Value("${ocp.passport.server}")
	private String serverUrl;

	@Override
	@Profiled(tag = "PassportServiceImpl.findPassportByAccountOrAliase")
	public PassportDto findPassportByAccountOrAliase(String passportOrAliase)
			throws PassportNotFoundException, PassportHasFrozenException, PassportHasLockedException, SystemException {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("account", passportOrAliase);
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				httpHeaders);
		ResponseEntity<OcpResponsePassportDto> responseEntity = null;
		try {
			long start_time = Calendar.getInstance().getTimeInMillis();
			logger.info("request: " + serverUrl + "/user/passport/query" + "," + request);
			responseEntity = restTemplate.postForEntity(serverUrl + "/user/passport/query", request,
					OcpResponsePassportDto.class);
			logger.info("response: " + serverUrl + "/user/passport/query" + "," + responseEntity);
			logger.info("response Time:" + (Calendar.getInstance().getTimeInMillis() - start_time));
		} catch (ResourceAccessException rae) {
			throw new SystemException("ResourceAccessException：" + rae.getMessage());
		} catch (Exception e) {
			throw new SystemException("Other Exception " + e.getMessage());
		}
		DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
		OcpResponsePassportDto responsePassportDto = responseEntity.getBody();
		if (responsePassportDto.getRespState().equals(ERROR_STATE)) {
			if (responsePassportDto.getCode().intValue() == 201001) {
				throw new PassportNotFoundException("Passport not found!");
			}
			if (responsePassportDto.getCode().intValue() == 201004) {
				throw new PassportHasLockedException("Passport has Locked");
			}
			if (responsePassportDto.getCode().intValue() == 201005) {
				throw new PassportHasLockedException("Passport has frozen");
			}
			OcpResponseDto responseDto = (OcpResponseDto) responsePassportDto;
			throw new SystemException("ocp-passport call exception: " + responseDto.toOcpString());
		}
		PassportDto passportDto = dozerBeanMapper.map(responsePassportDto, PassportDto.class);
		return passportDto;
	}

	@Override
	@Profiled(tag = "PassportServiceImpl.findPassportByAid")
	public PassportDto findPassportByAid(Long aid) throws PassportNotFoundException, SystemException {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("id", String.valueOf(aid));
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				httpHeaders);
		ResponseEntity<OcpResponsePassportDto> responseEntity = null;
		try {
			long start_time = Calendar.getInstance().getTimeInMillis();
			logger.info("request: " + serverUrl + "/user/passport/search/id" + "," + request);
			responseEntity = restTemplate.postForEntity(serverUrl + "/user/passport/search/id", request,
					OcpResponsePassportDto.class);
			logger.info("response: " + serverUrl + "/user/passport/search/id" + "," + responseEntity);
			logger.info("response Time:" + (Calendar.getInstance().getTimeInMillis() - start_time));
		} catch (ResourceAccessException rae) {
			throw new SystemException("ResourceAccessException：" + rae.getMessage());
		} catch (Exception e) {
			throw new SystemException("Other Exception " + e.getMessage());
		}
		DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
		OcpResponsePassportDto responsePassportDto = responseEntity.getBody();
		if (responsePassportDto.getRespState().equals(ERROR_STATE)) {
			if (responsePassportDto.getCode().intValue() == 201001) {
				throw new PassportNotFoundException("Passport not found!");
			}
			OcpResponseDto responseDto = (OcpResponseDto) responsePassportDto;
			throw new SystemException("ocp-passport call exception: " + responseDto.toOcpString());
		}
		PassportDto passportDto = dozerBeanMapper.map(responsePassportDto, PassportDto.class);
		return passportDto;
	}

	@Override
	public PassportDto findIsFreeCardUser(Long aid) throws SystemException {
		String params = "id=" + aid;
		OcpResponsePassportDto ocpResponsePassportDto = null;
		try {
			ocpResponsePassportDto = ocpPassportGatewayClient.findFreeCardUserByAid(params,
					ocpPassportProfile.getAppid(), ocpPassportProfile.getPwd());
		} catch(Exception e) {
			logger.error("Remote Call Exception ", e);
			throw new SystemException("Other Exception " + e.getMessage());
		}
		if(ocpResponsePassportDto.getRespState().equals(ERROR_STATE)) {
			logger.error("/user/passport/find/id call response:" + ocpResponsePassportDto.toOcpString());
			return null;
		}
		DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
		PassportDto passportDto = dozerBeanMapper.map(ocpResponsePassportDto, PassportDto.class);
		return passportDto;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
