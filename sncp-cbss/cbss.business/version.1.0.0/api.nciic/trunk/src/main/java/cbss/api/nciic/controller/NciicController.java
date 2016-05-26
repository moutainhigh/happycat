package cbss.api.nciic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cbss.core.authorize.AccessAuthorizeFilterConfigures;
import cbss.core.errorcode.EchoInfo;
import cbss.core.errorcode.ErrorCode;

import com.woniu.sncp.alarm.service.AlarmMessageService;
import com.woniu.sncp.nciic.dto.NciicMessageIn;
import com.woniu.sncp.nciic.dto.NciicMessageOut;
import com.woniu.sncp.nciic.service.NciicException;
import com.woniu.sncp.nciic.service.NciicMessageService;

@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class NciicController {
	@Autowired
	private ErrorCode errorCode;
	@Autowired
	private AlarmMessageService alarmMessageService;
	@Autowired
	private NciicMessageService nciicMessageService;

	@RequestMapping(value = "/nciic/query", method = RequestMethod.POST)
	@ResponseBody
	public EchoInfo<Object> query(@RequestBody NciicRequestDatas requestDatas) {
		return errorCode.getErrorCode(0,requestDatas.getSessionId());
	}

	@RequestMapping(value = "/nciic/deliver", method = RequestMethod.POST)
	@ResponseBody
	public EchoInfo<Object> deliver(@RequestBody NciicRequestDatas requestDatas) {

		NciicMessageIn nciicMessageIn = new NciicMessageIn(requestDatas.getParamdata().getRealName(), requestDatas.getParamdata().getIdentityNo());
		try {
			NciicMessageOut out = nciicMessageService.checkRealNameIdentityNo(nciicMessageIn);
			return errorCode.getErrorCode(1,requestDatas.getSessionId()).setData(out.actualResult());
		} catch (NciicException e) {
			return errorCode.getErrorCode(0,requestDatas.getSessionId());
		}

	}

}
