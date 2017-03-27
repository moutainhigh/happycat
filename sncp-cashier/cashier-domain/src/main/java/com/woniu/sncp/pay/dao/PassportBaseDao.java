package com.woniu.sncp.pay.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * 所有业务数据的基础访问接口，提供最常用的数据操作
 * 
 * @author YangHao
 * @since 2008-12-6 下午09:00:38
 * @contact <a href="mailto:hao.yh@qq.com">hao.yh@qq.com</a>
 * @copyright Woniu.com
 */
public interface PassportBaseDao {

	/**
	 * 使用带参数的语句进行查询
	 * 
	 * @param queryString
	 *            查询语句
	 * @param paramValues
	 *            键值对参数，<b>可以为null</b>
	 * @return
	 * @throws DataAccessException
	 */
	List<?> findAll(String queryString, Map<String, Object> paramValues) throws DataAccessException;

	/**
	 * 根据条件分页查询记录，<B><font color="red">查询条件中必须包含唯一排序，orderBy</font></b><br />
	 * 对于Hibernate实现，如果返回的是单个对象List，则可强制类型转化，例如：<br />
	 * return (List<ImprestBonus>) this.imprestBonusDao.page(hql, params,
	 * firstResult, maxResults, null, false);<br />
	 * 
	 * @param queryString
	 * @param paramValues
	 *            <参数格式 -- : >
	 * @param firstRowNum
	 *            每页开始记录索引值，第一个为0
	 * @param pageRowCount
	 *            每页记录个数
	 * @return
	 * @throws DataAccessException
	 */
	List<?> page(String queryString, Map<String, Object> paramValues, int firstRowNum, int pageRowCount)
			throws DataAccessException;

	/**
	 * 使用带参数的语句查询记录条数<br />
	 * 例如：select count(*) from t_table where ....
	 * 
	 * @param queryString
	 * @param paramValues
	 *            <参数格式 -- : >
	 * @return
	 */
	int count(String queryString, Map<String, Object> paramValues) throws DataAccessException;

	/**
	 * 调用存储过程 默认返回错误码：piResult，返回错误消息：psErrDesc
	 * 
	 * @param spName
	 *            过程名
	 * @param parameters
	 *            参数，注意日期型使用 new
	 *            java.sql.Timestamp.Timestamp.Timestamp(idate.getTime())
	 * @param outParams
	 *            输出参数，如 outparam1=TYPES.VARCHAR2
	 * 
	 * @param cursorName
	 *            返回游标名
	 * @return 第0个元素返回非游标数据，如：错误码：piResult，错误消息：psErrDesc <br />
	 *         第1个元素开始返回游标数据集合，内为键值对：TreeMap
	 * @throws DataAccessException
	 */
	List<Map<String, Object>> executeWithResult(String spName, Map<String, Object> parameters,
			Map<String, Integer> outParams, String cursorName) throws DataAccessException;

	/**
	 * 调用存储过程 默认返回错误码：piResult，返回错误消息：psErrDesc
	 * 
	 * @param spName
	 *            过程名
	 * @param inParams
	 *            输入参数，注意日期型使用 new java.sql.Timestamp.Timestamp.Timestamp(idate.getTime())
	 * @param outParamsType
	 *            输出参数类型，如 TYPES.VARCHAR ;如果inOutParams不为空，同样需要在这里指定inOutParams的输出类型.
	 * @param inOutParams
	 *            输入输出参数,过程中的IN OUT参数
	 * @param procParam
	 *            过程使用的参数名，和 存储过程参数名称顺序一致
	 * @param cursorName
	 *            返回游标名
	 * @return 第0个元素返回非游标数据，如：错误码：piResult，错误消息：psErrDesc <br />
	 *         第1个元素开始返回游标数据集合，内为键值对：TreeMap
	 * @throws DataAccessException
	 */
	List<Map<String, Object>> executeWithResult(String spName, Map<String, Object> inParams,
			Map<String, Integer> outParamsType, Map<String, Object> inOutParams, ArrayList<String> procParam,
			String cursorName) throws DataAccessException;
	/**
	 * 调用存储过程 默认返回错误码：piResult，返回错误消息：psErrDesc
	 * 
	 * @param spName
	 *            过程名
	 * @param parameters
	 *            参数，注意日期型使用 new
	 *            java.sql.Timestamp.Timestamp.Timestamp(idate.getTime())
	 * @param outParams
	 *            输出参数，如 outparam1=TYPES.VARCHAR2
	 * @param cursorName
	 *            返回游标名
	 * @param procCode
	 *            过程中过程编码名称 - 原来是 piResult
	 * @param procInfo
	 *            过程中过程过程信息名称 - 原来是psErrorInfo
	 * @return 第0个元素返回非游标数据，如：错误码：piResult，错误消息：psErrDesc <br />
	 *         第1个元素开始返回游标数据集合，内为键值对：TreeMap
	 */
	// public List<Map<String, Object>> executeWithResult(String spName,
	// Map<String, Object> parameters,
	// Map<String, Integer> outParams, String cursorName, String procCode,
	// String procInfo)
	// throws DataAccessException;
}
