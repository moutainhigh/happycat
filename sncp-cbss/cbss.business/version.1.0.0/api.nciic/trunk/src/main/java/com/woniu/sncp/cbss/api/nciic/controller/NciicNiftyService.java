package com.woniu.sncp.cbss.api.nciic.controller;

import java.util.UUID;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.bigdullrock.spring.boot.nifty.NiftyHandler;
import com.woniu.sncp.alarm.service.AlarmMessageService;
import com.woniu.sncp.cbss.api.core.thrift.Access;
import com.woniu.sncp.cbss.api.core.thrift.ApiConstants;
import com.woniu.sncp.cbss.api.core.thrift.Data;
import com.woniu.sncp.cbss.api.core.thrift.Echo;
import com.woniu.sncp.cbss.api.core.thrift.Signature;
import com.woniu.sncp.cbss.api.core.thrift.State;
import com.woniu.sncp.cbss.api.core.thrift.Status;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.nciic.dto.NciicMessageIn;
import com.woniu.sncp.nciic.service.NciicMessageService;

@NiftyHandler
public class NciicNiftyService implements com.woniu.sncp.cbss.api.core.thrift.Api.Iface {

	@Autowired
	private ErrorCode errorCode;
	@Autowired
	private AlarmMessageService alarmMessageService;
	@Autowired
	private NciicMessageService nciicMessageService;

	@Override
	public Echo invoke(Access access, Data data, Signature signature)
			throws TException {

		String classname = data.getParam().getClassname();
		if (NciicRequestParam.class.getName().equals(classname)) {
			NciicRequestParam nciicRequestParam = JSONObject.parseObject(data.getParam().getParam(), NciicRequestParam.class);
			NciicMessageIn nciicMessageIn = new NciicMessageIn(nciicRequestParam.getRealName(), nciicRequestParam.getIdentityNo());
			// try {
			// NciicMessageOut out =
			// nciicMessageService.checkRealNameIdentityNo(nciicMessageIn);
			return errorCode.getCode(1);
			// // } catch (NciicException e) {
			// return errorCode.getCode(0);
			// }
		} else {
			int resolvetype = data.getParam().getResolveType();
			Echo echo = new Echo(1L, data.getParam().getParam(), UUID.randomUUID().toString(), data.getVersion(), System.currentTimeMillis(), 1, new State(Status.SERVER_ALIVE, "", ""),
					ApiConstants.ECHO_DATA_RESOLVE_TYPE_DEFAULT);
			return echo;
		}
	}

	@Override
	public Echo status(Access access, Data data, Signature signature)
			throws TException {
		return null;
	}

	@Override
	public void over(Access access, Data data, Signature signature)
			throws TException {

	}

}
