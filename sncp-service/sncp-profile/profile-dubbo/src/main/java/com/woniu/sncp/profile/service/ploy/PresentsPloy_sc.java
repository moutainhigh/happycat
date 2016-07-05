package com.woniu.sncp.profile.service.ploy;
import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 首充面值活动 
 * 
 * @author luzz
 *
 */
@Service("presentsPloy_sc")
public class PresentsPloy_sc implements PresentsPloy {

	

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}


}
