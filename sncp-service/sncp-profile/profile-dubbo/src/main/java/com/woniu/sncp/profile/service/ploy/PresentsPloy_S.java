package com.woniu.sncp.profile.service.ploy;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 1.普通充值赠送（无限制） 2.充值10元赠送 3.卡类型活动
 * 
 * @author yanghao
 * @since 2010-7-12
 * 
 */
@Service("presentsPloy_S")
public class PresentsPloy_S implements PresentsPloy {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
