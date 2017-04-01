package com.woniu.sncp.pay.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.woniu.sncp.jdbc.batch.PreparedStatementResultSetHandle;

/**
 * 提供基于Session的业务数据操作接口，功能比较全面<br />
 * 所有CRUD(创建，读取，修改和删除)基本数据的操作在这个接口中，都是独立的<br />
 * 所有的DAO都可以使用这些基本操作<br />
 * -----------------------------------------------------------<br />
 * <b><font color="red">DAO实现类需要注入DataSource</font></b><br />
 * -----------------------------------------------------------<br />
 * 
 * @author YangHao
 * @since 2008-12-6 下午07:52:14
 * @contact <a href="mailto:hao.yh@qq.com">mailto:hao.yh@qq.com</a>
 * @copyright Woniu.com
 */
public interface BaseSessionDAO extends BaseDao {

	/**
	 * 根据条件分页查询记录，<b>（默认）第二排序：ID降序</b>
	 * @param queryString
	 * @param paramValues
	 * @param firstRowNum
	 * @param pageRowCount
	 * @param orderBy
	 * @param isAsc
	 * @param limit   mysql使用
	 * @param pageNum mysql使用
	 * @return
	 * @throws DataAccessException
	 */
	public List<?> page(String queryString, Map<String, Object> paramValues, int pageRowCount,int pageNum, 
			String orderBy, boolean isAsc,Class<?> elementType) throws DataAccessException;
	
	/**
	 * 根据条件分页查询记录，<b>（默认）第二排序：ID降序</b>
	 * 
	 * @param queryString
	 * @param paramValues
	 *            <参数格式 -- : >
	 * @param firstRowNum
	 *            每页开始记录索引值，第一个为0
	 * @param pageRowCount
	 *            每页记录个数
	 * @param orderBy
	 *            排序pojo属性，允许为null，<b>（默认）第二排序：ID降序</b>
	 * @param isAsc
	 *            若orderBy为null，则该属性失效
	 * @return 若采用sql方式查询，发挥list，每一行为一个键值对Map，键为sql语句中查询结果（和数据库一致）
	 * @throws DataAccessException
	 */
	List<?> page(String queryString, Map<String, Object> paramValues, int firstRowNum, int pageRowCount,
			String orderBy, boolean isAsc) throws DataAccessException;

	/**
	 * 查询sql 返回Long
	 * 
	 * @param sql
	 * @return
	 * @throws DataAccessException
	 */
	Long findForLong(String sql) throws DataAccessException;

	/**
	 * 调用存储过程，调用父类executeWithResult方法，传入参数为一定格式的json串
	 * 默认返回错误码：piResult，返回错误消息：psErrDesc
	 * 
	 * @param json
	 *            一定格式的json
	 *            {dbid:1,procName:"SN_ACCOUNT.ACC....",inParams:{"psAid"
	 *            :9001,....},outParams:{"psResult":1,....}}
	 * 
	 * @return 第0个元素返回非游标数据，如：错误码：piResult，错误消息：psErrDesc <br />
	 *         第1个元素开始返回游标数据集合，内为键值对：TreeMap
	 * @throws DataAccessException
	 */
	List<Map<String, Object>> executeWithResult(String json) throws DataAccessException;

	/**
	 * 更新
	 * 
	 * @param sql
	 * @param inParams
	 * @return
	 * @throws DataAccessException
	 */
	public int update(String sql, Map<String, Object> inParams) throws DataAccessException;
	
	public Map<String, Object> jdbcOne(Connection connection, String sql, boolean isCloseConnect) throws Exception;
	
	public Map<String, Object> jdbcOne(String sql) throws Exception;
	
	public JdbcTemplate getMyJdbcTemplate();

    PreparedStatementResultSetHandle createPreparementResultSetHandle(String sql) throws Throwable;
    
    public List<Map<String, Object>> jdbcList(Connection connection, String sql, boolean isClose) throws Exception;
    
    public List<Map<String, Object>> jdbcList(String sql) throws Exception;
    
    public int jdbcUpdate(String sql) throws Exception;

	public int jdbcUpdate(String sql, List<Object> values) throws Exception;

	public int jdbcUpdate(Connection connection, String sql, boolean isCloseConnect, List<Object> values)
			throws Exception;
	
	public int update(String sql, SqlParameterSource paramSource) throws DataAccessException;
	
	public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType)throws DataAccessException;
	public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType);
}
