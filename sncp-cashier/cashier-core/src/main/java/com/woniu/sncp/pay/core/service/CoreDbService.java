package com.woniu.sncp.pay.core.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.dao.BaseSessionDAO;

/**
 * <p>
 * descrption:
 * </p>
 * 
 * @author fuzl
 * @date 2017年4月14日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Service("coreDbService")
public class CoreDbService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BaseSessionDAO sessionDAO;
	
	/**
	 * ONLINE:db机器正常
	 */
	public static final String DB_MACHINE_STATE_ONLINE = "ONLINE";
	
	/**
	 * 1 开启检查
	 */
	public static final String DB_OPEN_CHECK = "1";
	
	
	/**
	 * 通过db机器名称查询db机器状态
	 * @param machineName   机器名称
	 * @return
	 */
	public String getDbState(String machineName) {

		try {
			StringBuffer queryString = new StringBuffer("SELECT * FROM performance_schema.replication_group_members ");
			if (StringUtils.isNotBlank(machineName)) {
				queryString.append(" where MEMBER_HOST = '" + machineName + "'");
			}

			List<Map<String, Object>> maps = null;
			maps = sessionDAO.jdbcList(queryString.toString());

			if (null != maps && maps.size() > 0) {
				Map<String, Object> map = maps.get(0);
				if (map.containsKey("MEMBER_HOST") && map.get("MEMBER_HOST").equals(machineName)) {
					logger.info("当前机器状态:{}", map.get("MEMBER_STATE"));
					return ObjectUtils.toString(map.get("MEMBER_STATE"));
				}
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
		}

		return "";
	}
	
	/**
	 * 获取查询测试结果
	 * @return
	 */
	public String getQueryTestResult(){
		try {
			StringBuffer queryString = new StringBuffer("SELECT 1 ");

			List<Map<String, Object>> maps = null;
			maps = sessionDAO.jdbcList(queryString.toString());
			if (null != maps && maps.size() > 0) {
				Map<String, Object> map = maps.get(0);
				if (Integer.parseInt(ObjectUtils.toString(map.get("1"))) == 1 ) {
					logger.info("测试查询结果:{}", map.get("1"));
					return ObjectUtils.toString(map.get("1"));
				}
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
		}
		return "";
	}
}
