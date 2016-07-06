package com.woniu.sncp.profile.service.ploy;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

/**
 * <pre>充值配送活动2</pre>
 * 累积充值额度奖励，不可重复领取<br>
 * 按充值的档领取，比如100,200,500,1000 达到当前档赠送奖励只赠送一次<br>
 * 限制分区，限制游戏,不限制平台，限制时间<br>
 * @author sungs
 *
 */
@Service("presentsPloy_y")
public class PresentsPloy_Y implements PresentsPloy {
	
	private static String PloyType = "y";
	
	private static Long LOG_TYPE_ID = 126L;

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
