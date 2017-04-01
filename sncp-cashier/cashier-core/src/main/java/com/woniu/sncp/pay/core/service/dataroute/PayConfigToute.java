package com.woniu.sncp.pay.core.service.dataroute;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.dao.PayConfigRouteDao;
import com.woniu.sncp.pay.repository.pay.ConfigInfo;

/**
 * <p>descrption: 数据库表配置路由管理类</p>
 * 
 * @author fuzl
 * @date   2017年4月1日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Service("payConfigToute")
public class PayConfigToute {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	PayConfigRouteDao payConfigManageDao;
	
	
	//1.确定当前seq路由到哪张表后缀
	@SuppressWarnings("unchecked")
	public String getSuffixBySeq(Long seq){
		String tableIndex = "";
		String selectCfgSql = "select * from CONFIG_INFO where N_ENABLE = 1";
		List<ConfigInfo> configList = (List<ConfigInfo>) payConfigManageDao.queryListEntity(selectCfgSql, null, ConfigInfo.class);
		if(null!=configList && configList.size()>0){
			for(int i=0;i<configList.size();i++){
				ConfigInfo config = configList.get(i);
				if(config.getEnable() ==1 && config.getBeginNum() < seq && seq<config.getEndNum()){
					tableIndex = config.getTableIndex();
					break;
				}
			}
		}
		if(logger.isInfoEnabled())
			logger.info("获取配置,序列值[{}],表后缀[{}]",seq,tableIndex);
		return tableIndex;
	}
	
}
