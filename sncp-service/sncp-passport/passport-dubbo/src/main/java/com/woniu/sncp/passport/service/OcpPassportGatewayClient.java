package com.woniu.sncp.passport.service;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.woniu.sncp.passport.dto.OcpResponsePassportDto;


@FeignClient(name="passports")
public interface OcpPassportGatewayClient {

	/**
	 * 查询帐号是否是免卡用户
	 * @param id 参数格式: id=帐号ID
	 * @param appid 权限应用id
	 * @param pwd   权限应用密码
	 * @return 帐号信息
	 */
	@RequestMapping(value = "/user/passport/find/id", method = RequestMethod.POST,
			consumes = "application/x-www-form-urlencoded",
			headers = {"Content-Type=application/x-www-form-urlencoded"})
	public OcpResponsePassportDto findFreeCardUserByAid(@RequestBody String aid, @RequestHeader("H_APPID") String appid, @RequestHeader("H_PWD") String pwd);
}
