package com.woniu.sncp.pay.core.service;



import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.ocp.business.passport.PassportComponent;
import com.woniu.sncp.ocp.exception.BusinessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.holder.OCPPassportContextHolder;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.passport.PassportPresentsPloy;

@Service("corePassportService")
public class CorePassportServiceImpl implements CorePassportService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BaseSessionDAO sessionDao;
    
    @Autowired
    private PassportComponent passportComponent;
    
    @Autowired
    private OCPPassportContextHolder contextHolder;

	@Override
	public Passport queryPassport(String userName) throws BusinessException {
//		Assert.hasText(userName, "参数不允许为空");
//		userName = StringUtils.upperCase(StringUtils.trim(userName));
//		// 目前不能使用只读库查询.否则可能会因为后续操作没有重新切换数据源,而想操作读写库的时候,会出错
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_ACCOUNT_DB);
//		Passport pp = passportDao.findByProperty("account", userName);
//		 
//		if(pp == null){//别名
//			pp = passportDao.findByProperty("aliase", userName);
//		}
//		
//		return pp;
		
		return passportComponent.findPassportByAccount(contextHolder, userName);
	}

//	@Override
//	public Passport queryPassportByUserName(String userName,
//			PassportPresentsPloy passportPresentsPloy) throws ServiceException {
//		String accountLimit = StringUtils.substringBetween(
//				passportPresentsPloy.getOtherLimitContent() + "|",
//				"accountLimit:", "|");
//		// accountLimit :1 活动开始以后注册的新账户
//		// accountLimit :2 活动开始之前的注册的老账户
//		String hql = "from Passport t where t.account=:userName ";
//		if (StringUtils.equals("1", accountLimit)) {
//			hql += "  and t.createDate >= :start ";
//		}
//		if (StringUtils.equals("2", accountLimit)) {
//			hql += "  and t.createDate <= :start ";
//		}
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userName", userName.toUpperCase());
//		if (StringUtils.isNotBlank(accountLimit)) {
//			params.put("start", passportPresentsPloy.getStartDate());
//		}
//		List<?> list = null;
//		try {
//			list = passportDao.findAll(hql, params);
//			if (list != null && list.size() > 0) {
//				return (Passport) list.get(0);
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//			throw new ServiceException("查询帐号相关活动失败");
//		}
//		return null;
//	}
//
//	@Override
//	public Passport queryPassport(long aid) throws DataAccessException {
//		// 目前不能使用只读库查询.否则可能会因为后续操作没有重新切换数据源,而想操作读写库的时候,会出错
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_ACCOUNT_DB);
//		return passportDao.findById(aid);
//	}
	
	@Override
	public void validatepassport(Passport passport) throws ValidationException {
		Assert.notNull(passport, "用户帐号不存在");
		Assert.assertEquals("帐号被锁定或被冻结", Passport.STATE_NORMAL, passport.getState());
	}

    @Override
    public int updateTimingTask(long taskId, String state) throws DataAccessException {
        // 更改中心库异步任务状态
        DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
        String sql = "update SN_PASSPORT.PP_TIMING_TASK set s_state = ?, d_modify = sysdate where n_id = ?";
        int result = sessionDao.getMyJdbcTemplate().update(sql, new Object[] { state, taskId });

        if (result != 1)
            logger.error("更改中心库TimingTask状态失败:taskId:" + taskId + ",更新数据量:" + result);

        return result;
    }

	@Override
	public Passport queryPassportByUserName(String userName, PassportPresentsPloy passportPresentsPloy)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Passport queryPassport(long aid) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}
