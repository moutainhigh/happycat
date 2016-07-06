package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * <pre>充值配送活动1</pre>
 * 累积充值额度奖励，可重复领取<br>
 * 设置累计充值档，比如50,每达到50赠送奖励，可无限赠送<br>
 * 限制分区，限制游戏,不限制平台，限制时间<br>
 * @author sungs
 *
 */
@Service("presentsPloy_z")
public class PresentsPloy_Z implements PresentsPloy {
	
	private static String PloyType = "z";
	
	private static Long LOG_TYPE_ID = 125L;

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
