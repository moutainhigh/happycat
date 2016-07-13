package com.woniu.sncp.cbss.api.fcm.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.json.JSON;
import com.bigdullrock.spring.boot.nifty.NiftyHandler;
import com.woniu.sncp.alarm.service.AlarmMessageService;
import com.woniu.sncp.cbss.api.core.thrift.Access;
import com.woniu.sncp.cbss.api.core.thrift.Data;
import com.woniu.sncp.cbss.api.core.thrift.Echo;
import com.woniu.sncp.cbss.api.core.thrift.Signature;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.fcm.dto.FcmGameProfileTo;
import com.woniu.sncp.fcm.service.FcmGameProfileService;
import com.woniu.sncp.fcm.service.FcmService;

@NiftyHandler
public class FcmApiService implements com.woniu.sncp.cbss.api.core.thrift.Api.Iface {
	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	@Autowired
	private FcmService fcmService;
	@Autowired
	private FcmGameProfileService fcmGameProfileService;
	@Autowired
	private ErrorCode errorCode;
	@Autowired
	private AlarmMessageService alarmMessageService;

	@Override
	public Echo invoke(Access access, Data data, Signature signature)
			throws TException {
		Class<?> object;
		try {
			object = Class.forName(data.getParam().getClassname());
			if (object.getName().equals(FcmConfRequestParam.class.getName())) {
				FcmConfRequestParam data1 = JSON.parse(data.getParam().getParam(), FcmConfRequestParam.class);
				long issuerId = data1.getIssuerId();
				String gameIds = data1.getGameIds();

				if (StringUtils.isBlank(gameIds) || issuerId <= 0L) {
					return errorCode.getCode(1001);
				}

				String[] gameArr = gameIds.split(",");
				if (gameArr == null || gameArr.length < 1) {
					return errorCode.getCode(1001);
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
					return errorCode.getCode(1);
				} catch (MissingParamsException e) {
					logger.error("fcmConf", e);
					return errorCode.getCode(10001);
				} catch (Exception e) {
					logger.error("fcmConf", e);
					return errorCode.getCode(10002);
				}
			} else {
				return errorCode.getCode(-1000);
			}
		} catch (Exception e1) {
			return errorCode.getCode(-1001, e1.getMessage());
		}
	}

	@Override
	public void over(Access access, Data data, Signature signature)
			throws TException {

	}

	@Override
	public Echo status(Access access, Data data, Signature signature)
			throws TException {
		return null;
	}
}
