package com.woniu.sncp.profile.service.ploy;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * -- “乐充”活动 - 道具放入<礼品盒 SN_ACCOUNT.ACC_GIFT_BOX> -- 按游戏做区分，每个游戏都是独立的乐充活动 --
 * s_limit_content
 * 放入最低充值金额限制(必须)、限制充入分区(可无)，以"|"分隔，格式：moneyLimit:10|areaLimit:9001,9002 --
 * SN_ACCOUNT.ACC_GIFT_BOX.N_RELATION_ID 放入的是充值记录ID --
 * 根据记录id和pp_presents_log可查出关联记录 -- 乐充类型：>>>> 活动是循环的，如年度奖励：本月获奖后第12个月才能获奖 -- K -
 * 单月充值奖励 - 该用户在付费月的上一个月为非付费月,本月充值大于或等于10元,永久保留 -- L - 季度连续充值奖励 -
 * 3个月连续充值大于或等于10元,永久保留 -- M - 年度连续充值奖励 - 3个月连续充值大于或等于10元,永久保留
 * 
 * @since 2010-7-19
 * 
 */
@Service("presentsPloy_L")
public class PresentsPloy_L implements PresentsPloy {

    // 活动类型
    private String PloyType = "L";

    private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

}
