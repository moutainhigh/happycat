package com.woniu.sncp.profile.service.ploy;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import java.util.Map;

/**
 * 
 * 基于普通充值赠送修改，能够根据页面面值/自定义面值的数量翻倍赠送道具
 * 
 * 总金额区间（包含边界）impmoney:1,100  相等于 1 <= impmoney <= 100
 * 面额区间（包含边界）valuemoney:1,100  相等于 1 <= valuemoney <= 100
 * 
 * 总金额区间和面额区间只能选一个，同时配只有总金额区间有效
 * 
 * 总金额和面额关系
 * 总金额=面额*数量
 * 
 */
@Service("presentsPloy_mz")
public class PresentsPloy_mz implements PresentsPloy {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
