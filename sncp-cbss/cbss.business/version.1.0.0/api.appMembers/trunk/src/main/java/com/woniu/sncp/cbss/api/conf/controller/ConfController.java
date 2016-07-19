package com.woniu.sncp.cbss.api.conf.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.woniu.sncp.cbss.api.util.PropertiesUtl;
import com.woniu.sncp.cbss.core.authorize.AccessAuthorizeFilterConfigures;
import com.woniu.sncp.cbss.core.authorize.rest.EchoRestControllerAspectType;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.exception.MissingParamsException;
import com.woniu.sncp.profile.dto.DownConfigTo;
import com.woniu.sncp.profile.dto.PaginationTo;
import com.woniu.sncp.profile.service.DownConfigService;

@RestController
@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)
@Configuration
public class ConfController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorCode.class);
	@Autowired
	private DownConfigService downConfigService;
	@Autowired
	private ErrorCode errorCode;
	@Autowired
	private PropertiesUtl propertiesUtl;
	
	/**
	 * 
	 * @param requestDatas
	 * @return
	 */
	@RequestMapping(value = "/app/members/conf", method = RequestMethod.POST)
	@ResponseBody
	@EchoRestControllerAspectType
	public EchoInfo<Object> appMembersConf(@RequestBody AppConfRequestDatas requestDatas) {
		AppConfRequestParam data = requestDatas.getParamdata();
		String type = data.getType();
		String osType = data.getOsType();
		int pageSize = data.getPageSize();
		int number = data.getPageNumber();

		if (StringUtils.isBlank(type) || StringUtils.isBlank(osType)) {
			return errorCode.getErrorCode(1001, requestDatas.getSessionId());
		}

		try {
			PaginationTo page = downConfigService.query(type, osType, pageSize, number);
			if(page == null){
				return errorCode.getErrorCode(10007, requestDatas.getSessionId());
			}
			EchoInfo<Object> result = errorCode.getErrorCode(1, requestDatas.getSessionId());
			String host = propertiesUtl.getUrl();
			List<DownConfigTo> downConfigList = page.getDownConfigList();
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			for (DownConfigTo to : downConfigList) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("type", to.getType());
				map.put("name", to.getName());
				map.put("icoUrl", host + to.getIco());
				map.put("downUrl", to.getDownUrl());
				map.put("desc", to.getDesc());
				map.put("osType", to.getOsType());
				list.add(map);
			}
			AppConfRequestParam d = new AppConfRequestParam();
			d.setPageNumber(page.getPageNumber());
			d.setPageSize(page.getPageSize());
			d.setTotalSize(page.getTotalSize());
			d.setList(list);
			result.setData(d);
			return result;
		} catch (MissingParamsException e) {
			logger.error("appMembersConf", e);
			return errorCode.getErrorCode(10001, requestDatas.getSessionId());
		} catch (Exception e) {
			logger.error("appMembersConf", e);
			return errorCode.getErrorCode(10002, requestDatas.getSessionId());
		}
	}
}
