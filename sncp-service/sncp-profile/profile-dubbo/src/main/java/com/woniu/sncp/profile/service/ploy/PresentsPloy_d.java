package com.woniu.sncp.profile.service.ploy;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 武魂之石
 * 1.当前的分区是否已经存在武魂
 * 2.累计充值超过100
 * 
 * @author mzhang
 * @since 2010-7-19
 * 
 */
@Service("presentsPloy_d")
public class PresentsPloy_d implements PresentsPloy {

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
