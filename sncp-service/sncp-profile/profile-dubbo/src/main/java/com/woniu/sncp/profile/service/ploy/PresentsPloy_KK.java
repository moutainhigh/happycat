package com.woniu.sncp.profile.service.ploy;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import java.util.*;

/**
 * VIP升星礼包
 * @author congyj
 *
 */
@Service("presentsPloy_k")
public class PresentsPloy_KK implements PresentsPloy {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
