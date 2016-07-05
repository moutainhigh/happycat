package com.woniu.sncp.profile.service.ploy;

import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * 1.普通充值赠送（无限制） 2.充值10元赠送 3.卡类型活动
 * 
 * @author mizy
 * 
 */
@Service("presentsPloy_q")
public class PresentsPloy_q implements PresentsPloy {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	
	/** 单笔充值活动类型*/
	public static final long LOG_TYPE = 100;


	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
