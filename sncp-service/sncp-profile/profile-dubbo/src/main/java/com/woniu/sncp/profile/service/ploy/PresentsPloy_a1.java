package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * 充值返点
 * @author mizy
 *
 */
@Service("presentsPloy_a1")
public class PresentsPloy_a1 implements PresentsPloy {

	//充值返点类型
	public static final long TYPE_148 = 148;

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
