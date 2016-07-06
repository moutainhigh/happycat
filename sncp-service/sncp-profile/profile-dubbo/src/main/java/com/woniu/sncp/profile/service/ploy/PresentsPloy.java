package com.woniu.sncp.profile.service.ploy;


import org.springframework.dao.DataAccessException;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import java.util.Map;

/**
 * 活动接口 - 所有活动的父接口
 *
 * @author yanghao
 * @since 2010-7-27
 *
 */
public interface PresentsPloy {

    /**
     * 活动查询
     *
     * @param ploy
     *            活动POJO
     * @param params
     *            参数
     * @param isQuery
     *            是否查询<br />
     *            true-查询赠送道具，false - 查看下方return注释
     * @throws DataAccessException
     * @throws ValidationException
     * @return List<[propsId,propsCode,name,gameId,state,amount,limitCondition]> <br />
     * 			List<{@link LargessProps}> <br />
     * 			{@link PloyBusinessLog} <br />
     * 			即：[返回赠送道具，返回系统送道具记录表sn_imprest.imp_largess_info，插入日志SN_SALES.BUSINESS_LOG{@link PloyBusinessLog}]
     */
    public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
            throws DataAccessException;
}

