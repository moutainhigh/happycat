package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * 移动首充活动-主动领取方式
 * 
 * @author yanghao
 * @since 2010-7-19
 * 
 */
@Service("presentsPloy_YDC")
public class PresentsPloy_YDC implements PresentsPloy {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
