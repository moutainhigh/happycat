package com.woniu.sncp.pay.core.service;

import javax.xml.rpc.ServiceException;

import org.springframework.dao.DataAccessException;

import com.woniu.sncp.ocp.exception.BusinessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.passport.PassportPresentsPloy;

/**
 * 公共Passport业务
 * 
 * @author yanghao 2010-4-13
 * 
 */
public interface CorePassportService {

	public static final String NA_OPER = "NA";
	public static final String IP_VAL = "IP_VAL";

	/**
	 * 保留
	 * 帐号查询
	 * 
	 * @param userName
	 * @return
	 * @throws DataAccessException
	 */
	Passport queryPassport(String userName) throws BusinessException;

	/**
	 * 保留
	 * 获取账号Passport
	 * 
	 * @param userName
	 *            账号
	 * @throws DataAccessException
	 */
	Passport queryPassportByUserName(String userName, PassportPresentsPloy passportPresentsPloy)
			throws ServiceException;
	
	/**
	 * 帐号查询
	 * 
	 * @param aid
	 * @return
	 * @throws DataAccessException
	 */
	Passport queryPassport(long aid) throws DataAccessException;
	
	/**
	 * 验证帐号
	 * 
	 * @param passport
	 * @throws ValidationException
	 *             帐号验证失败
	 */
	void validatepassport(Passport passport) throws ValidationException;


    /**
     * 更改<font color=red>中心库</font>TimingTask状态
     *
     * @param taskId
     * @param state
     * @return 更改数量
     * @throws DataAccessException
     */
    int updateTimingTask(long taskId, String state) throws DataAccessException;
}
