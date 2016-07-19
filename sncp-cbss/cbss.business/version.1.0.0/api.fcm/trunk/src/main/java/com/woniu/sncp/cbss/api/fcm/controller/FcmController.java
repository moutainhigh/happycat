package com.woniu.sncp.cbss.api.fcm.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.alarm.dto.AlarmMessageTo;
import com.woniu.sncp.alarm.service.AlarmMessageService;
import com.woniu.sncp.cbss.core.authorize.AccessAuthorizeFilterConfigures;
import com.woniu.sncp.cbss.core.authorize.rest.EchoRestControllerAspectType;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.cbss.core.model.access.SecurityResource;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;
import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;
import com.woniu.sncp.fcm.service.FcmGameProfileService;
import com.woniu.sncp.fcm.service.FcmService;
import com.woniu.sncp.passport.exception.PassportNotFoundException;

@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class FcmController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	@Autowired
	private FcmService fcmService;
	@Autowired
	private FcmGameProfileService fcmGameProfileService;
	@Autowired
	private ErrorCode errorCode;
	@Autowired
	private AlarmMessageService alarmMessageService;

	/**
	 * eai中修改防沉迷开关后,调此接口把开关数据同步过去
	 * 
	 * @param requestDatas
	 * @return
	 */
	@RequestMapping(value = "/fcm/conf", method = RequestMethod.POST)
	@ResponseBody
	@EchoRestControllerAspectType
	public EchoInfo<Object> fcmConf(@RequestBody FcmConfRequestDatas requestDatas) {
		FcmConfRequestParam data = requestDatas.getParamdata();
		long issuerId = data.getIssuerId();
		String gameIds = data.getGameIds();

		if (StringUtils.isBlank(gameIds) || issuerId <= 0L) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}

		String[] gameArr = gameIds.split(",");
		if (gameArr == null || gameArr.length < 1) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}
		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < gameArr.length; i++) {
			ids.add(Long.valueOf(gameArr[i]));
		}
		try {
			List<Long> deleteList = new ArrayList<Long>();
			List<FcmGameProfileTo> profiles = fcmGameProfileService.query(issuerId);
			for (int i = 0; profiles != null && i < profiles.size(); i++) {
				FcmGameProfileTo to = profiles.get(i);
				Long gameId = to.getGameId();
				if (gameId != null && gameId.longValue() < 0L) {
					if (!ids.contains(gameId)) {// 查出来的防沉迷游戏,已经不在现在新配置的列表中,需要删除
						deleteList.add(gameId);
					}
				}
			}
			for (Long deleteFcm : deleteList) {
				fcmGameProfileService.delete(issuerId, deleteFcm);
			}
			for (Long id : ids) {
				FcmGameProfileTo to = fcmGameProfileService.query(issuerId, id);
				if (to != null) {
					continue;
				}
				to = new FcmGameProfileTo();
				to.setAoId(issuerId);
				to.setGameId(id);
				fcmGameProfileService.save(to);
			}
			return errorCode.getErrorCode(1, requestDatas.getSessionId());
		} catch (MissingParamsException e) {
			logger.error("fcmConf", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("fcmConf", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
	}

	@RequestMapping(value = "/fcm/queryTime", method = RequestMethod.POST)
	@ResponseBody
	@EchoRestControllerAspectType
	public EchoInfo<Object> queryTime(@RequestBody FcmTimeRequestDatas requestDatas) {
		FcmTimeRequestParam data = requestDatas.getParamdata();
		long gameId = data.getGameId();
		long aid = data.getAid();

		if (aid <= 0L || gameId <= 0L) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}

		try {
			String identity = fcmService.queryIdentity(aid);
			PassportFcmTotalTimeTo passportFcmTotalTimeTo = fcmService.queryUserFcmTotalTime(identity, gameId);
			EchoInfo<Object> result = errorCode.getErrorCode(1, requestDatas.getSessionId());
			FcmTimeRequestParam d = new FcmTimeRequestParam();
			if (passportFcmTotalTimeTo == null) {
				d.setLeaveTime(0L);
				d.setTime(0L);
			} else {
				d.setLeaveTime(passportFcmTotalTimeTo.getLeaveTime());
				d.setTime(passportFcmTotalTimeTo.getTime());
			}
			result.setData(d);
			return result;
		} catch (MissingParamsException e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (PassportNotFoundException e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(13101, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
	}

	@RequestMapping(value = "/fcm/updateTime", method = RequestMethod.POST)
	@ResponseBody
	@EchoRestControllerAspectType
	public EchoInfo<Object> updateTime(@RequestBody FcmTimeRequestDatas requestDatas) {
		FcmTimeRequestParam data = requestDatas.getParamdata();
		long gameId = data.getGameId();
		long aid = data.getAid();
		Long leaveTime = data.getLeaveTime();
		Long time = data.getTime();

		if (aid <= 0L || gameId <= 0L) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}

		try {
			String identity = fcmService.queryIdentity(aid);
			PassportFcmTotalTimeTo passportFcmTotalTimeTo = fcmService.queryUserFcmTotalTime(identity, gameId);
			if (passportFcmTotalTimeTo == null) {
				passportFcmTotalTimeTo = new PassportFcmTotalTimeTo();
				passportFcmTotalTimeTo.setGameId(gameId);
				passportFcmTotalTimeTo.setIdentity(identity);
			}
			passportFcmTotalTimeTo.setLastChange(new Date());
			if (leaveTime != null) {
				passportFcmTotalTimeTo.setLeaveTime(leaveTime);
			}
			if (time != null) {
				passportFcmTotalTimeTo.setTime(time);
			}
			fcmService.save(passportFcmTotalTimeTo);
			return errorCode.getErrorCode(1, requestDatas.getSessionId());
		} catch (MissingParamsException e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (PassportNotFoundException e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(13101, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("updateTime", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/fcm/onlinetime", method = RequestMethod.POST)
	@ResponseBody
	@EchoRestControllerAspectType
	public EchoInfo<Object> fcmOnlineTime(@RequestBody FcmRequestDatas requestDatas) {
		FcmOnlinetimeRequestParam data = requestDatas.getParamdata();
		long aid = data.getAid();
		long issuerId = data.getIssuerId();
		long gameId = data.getGameId();

		if (aid <= 0L || issuerId <= 0L || gameId <= 0L) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}

		try {
			SecurityResource resource = requestDatas.getSecurityResource();
			boolean validateThreeCondition = false;
			if (resource != null) {
				Map<String, Object> map = resource.getNoteFirst();
				validateThreeCondition = Boolean.valueOf(ObjectUtils.toString(map.get("validateThree")));
			}

			long start1 = System.currentTimeMillis();
			boolean isfcm = fcmService.isFcm(aid, issuerId, gameId, validateThreeCondition);
			long start2 = System.currentTimeMillis();
			if (isfcm) {
				String identity = fcmService.queryIdentity(aid);
				long start3 = System.currentTimeMillis();
				Long time = fcmService.fcmOnlineTime(identity, gameId);
				long start4 = System.currentTimeMillis();
				logger.info((start2 - start1) + "," + (start3 - start2) + "," + (start4 - start3) + "aid:" + aid + ",issuerId:" + issuerId + ",gameId:" + gameId + ",isfcm:" + isfcm + ",identity:"
						+ identity + ",time:" + time);
				Map<String, Object> data1 = new HashMap<String, Object>();
				data1.put("onlineTime", time);
				return errorCode.getErrorCode(1, requestDatas.getSessionId()).setData(data1);
			} else {
				logger.info((start2 - start1) + ",-1,-1" + "aid:" + aid + ",issuerId:" + issuerId + ",gameId:" + gameId + ",isfcm:" + isfcm + ",identity:,time:");
				return errorCode.getErrorCode(10017, requestDatas.getSessionId());
			}
		} catch (MissingParamsException e) {
			logger.error("fcmOnlineTime", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (PassportNotFoundException e) {
			logger.error("fcmOnlineTime", e);
			return errorCode.getErrorCode(13101, requestDatas.getSessionId());
		} catch (Exception e) {
			AlarmMessageTo to = new AlarmMessageTo("", e.getMessage());
			alarmMessageService.sendMessage(to);
			logger.error("fcmOnlineTime", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
	}
}
