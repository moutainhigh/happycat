package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * 充值累计返点活动<br/>
 * 当日充值金额达2000元，官方再赠送2%的点数；超过2000元，每赠加1000元，
 * 点数赠送按1%累加，赠点比例达到10%后，需再次充值10000元方可获得11%的点数赠送，
 * 以此类推，充的越多，送的越多。（未满足整数金额，按实际金额计算）
 * 限制条件是:Level:2000-3000:0.02,3000-10000:0.03,20000-max:0.01|ployDay:0
 * 
 * @author zhangms
 * 
 */
@Service("presentsPloy_e")
public class PresentsPloy_e implements PresentsPloy {
	
	private static String PloyType = "e";

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
