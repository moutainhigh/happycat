package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * 特殊订单赠道具
 * @author mizy
 *
 */
@Service("presentsPloy_a3")
public class PresentsPloy_a3 implements PresentsPloy {

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}
