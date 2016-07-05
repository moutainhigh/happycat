package com.woniu.sncp.profile.service.ploy;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.profile.po.PassportPresentsPloyPo;

import javax.annotation.Resource;
import java.util.*;

/**
 * 首充活动
 * 
 * @author yanghao
 * @since 2010-7-19
 * 
 */
@Service("presentsPloy_C")
public class PresentsPloy_C implements PresentsPloy {

	@Override
	public Object[] doPloyQuery(PassportPresentsPloyPo ploy, Map<String, Object> params, boolean isQuery)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}


}
