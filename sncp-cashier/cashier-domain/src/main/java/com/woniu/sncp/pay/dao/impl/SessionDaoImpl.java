package com.woniu.sncp.pay.dao.impl;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import com.woniu.sncp.jdbc.batch.BatchHandle;
import com.woniu.sncp.jdbc.batch.PreparedStatementResultSetHandle;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.dao.JdbcHelper;
import com.woniu.sncp.pay.dao.PayBaseDao;

/**
 * <p>
 * 基于Spring的SimpleJdbcTemplate模板的具体数据库操作实现
 * <p>
 * -----------------------------------------------------------
 * <p>
 * <b><font color="red">DAO实现类已通过Autowired方式注入{@link DataSource}</font></b>
 * <p>
 * <b><font color="red">DAO实现类可通过get***Template()获得各个Template对象</font></b>
 * <p>
 * -----------------------------------------------------------
 * 
 * @author YangHao
 * @since 2009-1-12 下午03:17:04
 * @Contact <a href="mailto:hao.yh@qq.com">hao.yh@qq.com</a>
 * @copyright Woniu.com
 */
@Repository
public class SessionDaoImpl implements BaseSessionDAO {

	// log4j日志
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected JdbcTemplate jdbcTemplate;
	

	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	// 数组容量
	private static short LOBCAP = 128;

	// ***************************************************************************
	// * ------------------------------具体实现方法------------------------------ *
	// **************************************************************************/

	/**
	 * 返回结果为List，每个List包括键值对Map，Key为Sql查询语句的列名
	 */
	public List<Map<String, Object>> findAll(String queryString, Map<String, Object> paramValues)
			throws DataAccessException {
		List<Map<String, Object>> result = this.namedParameterJdbcTemplate.queryForList(queryString, paramValues);
		logger.debug("传入sql：" + queryString);
		logger.debug("传入键值对参数：" + paramValues);
		return result;
	}

	@Override
	public List<?> page(String queryString, Map<String, Object> paramValues, int firstRowNum, int pageRowCount)
			throws DataAccessException {
		if (MapUtils.isEmpty(paramValues)) {
			//throw new Cm4jDaoException("查询参数不允许为空");
		}
		StringBuilder sqlBuilder = new StringBuilder("select * from (select row_.*,rownum rownum_ from (").append(
				queryString).append(") row_ where rownum <= :maxRowNum) where rownum_> :firstRowNum");
		paramValues.put("firstRowNum", firstRowNum);
		paramValues.put("maxRowNum", firstRowNum + pageRowCount);
		logger.debug("传入sql：" + sqlBuilder.toString());
		logger.debug("传入键值对参数：" + paramValues);
		return this.namedParameterJdbcTemplate.queryForList(sqlBuilder.toString(), paramValues);
	}

	@Override
	public List<?> page(String queryString, Map<String, Object> paramValues, int firstRowNum, int pageRowCount,
			String orderBy, boolean isAsc) throws DataAccessException {
		if (MapUtils.isEmpty(paramValues)) {
			//throw new Cm4jDaoException("查询参数不允许为空");
		}
		StringBuilder sqlBuilder = new StringBuilder("select * from (select row_.*,rownum rownum_ from (").append(
				queryString).append(") row_ where rownum <= :maxRowNum) where rownum_> :firstRowNum");
		sqlBuilder.append(" ORDER BY ").append(orderBy).append(isAsc ? " ASC" : " DESC");
		paramValues.put("firstRowNum", firstRowNum);
		paramValues.put("maxRowNum", firstRowNum + pageRowCount);

		logger.debug("传入sql：" + sqlBuilder.toString());
		logger.debug("传入键值对参数：" + paramValues);
		return this.namedParameterJdbcTemplate.queryForList(sqlBuilder.toString(), paramValues);
	}

	@Override
	public int count(String queryString, Map<String, Object> paramValues) throws DataAccessException {
		int result = 0;
		try {
			result = this.namedParameterJdbcTemplate.queryForObject(queryString, paramValues,Integer.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			logger.debug("查询记录数为0");
		}
		return result;
	}


	public List<Map<String, Object>> executeWithResult(String spName, Map<String, Object> parameters,
			Map<String, Integer> outParams, String cursorName) throws DataAccessException {

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

		if (spName == null) {
			//throw new Cm4jDaoException("存储过程名称不能为空");
		}

		CallableStatement cmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = jdbcTemplate.getDataSource().getConnection();
			if (logger.isInfoEnabled())
				logger.info("当前数据库：" + conn.getMetaData().getURL());
		} catch (SQLException e) {
			logger.error("获取数据库链接异常", e);
			//throw new Cm4jDaoException("打开数据库连接异常", e);
		}
		int pCount = ((parameters == null) ? 0 : parameters.size()) + ((outParams == null) ? 0 : outParams.size());
		StringBuilder sb = new StringBuilder(40);

		sb.append("{call ").append(spName).append("(");
		for (int i = 0; i < pCount; i++) {
			if (i != 0)
				sb.append(",");
			sb.append("?");
		}
		sb.append(")}");

		String sql = sb.toString();
		Map<String, Object> ret = new TreeMap<String, Object>();
		try {
			cmt = conn.prepareCall(sql);
			if (parameters != null && parameters.size() > 0) {
				Set<String> keys = parameters.keySet();
				for (String key : keys) {
					Object value = parameters.get(key);
					if (value instanceof java.util.Date) {
						cmt.setDate(key, new Date(((java.util.Date) value).getTime()));
					} else {
						cmt.setObject(key, value);
					}

				}
			}
			if (outParams != null && outParams.size() > 0) {
				Set<String> keys = outParams.keySet();
				for (String key : keys) {
					Integer value = outParams.get(key);
					cmt.registerOutParameter(key, value.intValue());
				}
			}
			try {
				cmt.execute();
			} catch (RuntimeException e) {
				logger.error("调用过程异常", e);
				//throw new Cm4jDaoException("调用过程异常", e);
			}
			if (outParams != null && outParams.size() > 0) {
				Set<String> keys = outParams.keySet();
				for (String key : keys) {
					if (key.equals(cursorName)) {
						continue;
					}
					Object value = cmt.getObject(key);
					if (value != null) {
						if (value instanceof Clob) {

							Reader reader = ((Clob) value).getCharacterStream();
							char[] cbuff = new char[LOBCAP];
							StringBuffer buffer = new StringBuffer();
							int index = 0;
							while ((index = reader.read(cbuff, index, cbuff.length)) >= 0) {
								buffer.append(new String(cbuff).trim());
								cbuff = new char[LOBCAP];
							}
							ret.put(key, buffer.toString().trim());

						} else if (value instanceof Blob) {

							InputStream reader = ((Blob) value).getBinaryStream();
							byte[] cbuff = new byte[LOBCAP];
							StringBuffer buffer = new StringBuffer();
							int index = 0;
							while ((index = reader.read(cbuff, index, cbuff.length)) >= 0) {
								buffer.append(new String(cbuff).trim());
								cbuff = new byte[LOBCAP];
							}
							ret.put(key, buffer.toString());

						} else {
							ret.put(key, value);
						}
					}
				}
			}
			// 将其他返回信息放入结果
			data.add(ret);

			// 将游标中数据放入结果
			if (StringUtils.isNotBlank(cursorName)) {
				try {
					rs = (ResultSet) cmt.getObject(cursorName);
				} catch (SQLException e) {
					logger.warn("存储过程没有游标可以打开");
					return data;
				}
				if (rs != null) {
					while (rs.next()) {
						Map<String, Object> row = new LinkedHashMap<String, Object>();
						int n = rs.getMetaData().getColumnCount();
						for (int i = 0; i < n; i++) {
							row.put(rs.getMetaData().getColumnName(i + 1), rs.getObject(i + 1));
						}
						data.add(row);
					}
				}
			}
		} catch (Throwable e) {
			logger.error("", e);
			throw new DataRetrievalFailureException("ERROR", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (cmt != null) {
					cmt.close();
					cmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();// 关闭异常
			}
		}
		return data;
	}


	public List<Map<String, Object>> executeWithResult(String procName, Connection conn, boolean isClose,
			Map<String, Object> inParams, Map<String, Integer> outParams, Map<String, Object> inOutParams,
			ArrayList<String> procParams, String cursorName) throws DataAccessException {

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

		if (procName == null) {
			//throw new Cm4jDaoException("存储过程名称不能为空");
		}
		if (inOutParams == null || inOutParams.isEmpty()) {
			//throw new Cm4jDaoException("InOut类型参数和参数值不能为空");
		}

		Set<String> iopns = inOutParams.keySet();
		for (String iopn : iopns) {
			if (!outParams.containsKey(iopn)) {
			//	throw new Cm4jDaoException("InOut类型参数,需要在输出参数里指定参数类型");
			}
		}

		CallableStatement cmt = null;
		ResultSet rs = null;
		try {
			if (logger.isInfoEnabled())
				logger.info("当前数据库：" + conn.getMetaData().getURL());
			if (conn.isClosed()) {
				conn = null;
				conn = jdbcTemplate.getDataSource().getConnection();
				if (logger.isInfoEnabled())
					logger.info("Closed Reconnection 当前数据库：" + conn.getMetaData().getURL());
			}
		} catch (SQLException e) {
			logger.error("获取数据库链接异常", e);
			//throw new Cm4jDaoException("打开数据库连接异常", e);
		}
		int pCount = ((procParams == null) ? 0 : procParams.size());
		StringBuilder sb = new StringBuilder(40);

		sb.append("{call ").append(procName).append("(");
		for (int i = 0; i < pCount; i++) {
			if (i != 0)
				sb.append(",");
			sb.append("?");
		}
		sb.append(")}");

		String sql = sb.toString();
		Map<String, Object> ret = new TreeMap<String, Object>();
		try {
			cmt = conn.prepareCall(sql);
			if (inParams != null && inParams.size() > 0) {

				for (int i = 0; i < procParams.size(); i++) {
					int index = i + 1;
					String procParam = procParams.get(i);
					Object value = inParams.get(procParam);
					if (value != null) {// 输入
						if (value instanceof java.util.Date) {
							cmt.setDate(index, new Date(((java.util.Date) value).getTime()));
						} else {
							cmt.setObject(index, value);
						}
						continue;
					}

					value = inOutParams.get(procParam);
					if (value != null) {// 输入输出
						if (value instanceof java.util.Date) {
							cmt.setDate(index, new Date(((java.util.Date) value).getTime()));
						} else {
							cmt.setObject(index, value);
						}
						value = outParams.get(procParam);
						if (value != null) {// 输出
							cmt.registerOutParameter(index, Integer.parseInt(String.valueOf(value)));
						} else {

							//throw new Cm4jDaoException("InOut类型参数,需要在输出参数里指定参数类型");
						}
						continue;
					}

					value = outParams.get(procParam);
					if (value != null) {// 输出
						cmt.registerOutParameter(index, Integer.parseInt(String.valueOf(value)));
						continue;
					}

				}
			}
			try {
				cmt.execute();
			} catch (RuntimeException e) {
				logger.error("调用过程异常", e);
			///	throw new Cm4jDaoException("调用过程异常", e);
			}
			if (outParams != null && outParams.size() > 0) {
				for (int i = 0; i < procParams.size(); i++) {
					String procParam = procParams.get(i);
					Object value = outParams.get(procParam);
					if (value != null) {// 输出
						int index = i + 1;
						value = cmt.getObject(index);
						if (value != null) {
							if (value instanceof Clob) {
								Reader reader = ((Clob) value).getCharacterStream();
								char[] cbuff = new char[LOBCAP];
								StringBuffer buffer = new StringBuffer();
								int off = 0;
								while ((off = reader.read(cbuff, off, cbuff.length)) >= 0) {
									buffer.append(new String(cbuff).trim());
									cbuff = new char[LOBCAP];
								}
								ret.put(procParam, buffer.toString().trim());
							} else if (value instanceof Blob) {
								InputStream reader = ((Blob) value).getBinaryStream();
								byte[] cbuff = new byte[LOBCAP];
								StringBuffer buffer = new StringBuffer();
								int off = 0;
								while ((off = reader.read(cbuff, off, cbuff.length)) >= 0) {
									buffer.append(new String(cbuff).trim());
									cbuff = new byte[LOBCAP];
								}
								ret.put(procParam, buffer.toString());
							} else {
								ret.put(procParam, value);
							}
						}
					}
				}
			}
			// 将其他返回信息放入结果
			data.add(ret);
			// 将游标中数据放入结果
			if (StringUtils.isNotBlank(cursorName)) {
				for (int j = 0; j < procParams.size(); j++) {
					String procParam = procParams.get(j);
					if (procParam.equals(cursorName)) {
						try {
							rs = (ResultSet) cmt.getObject(j + 1);
						} catch (SQLException e) {
							logger.warn("存储过程没有游标可以打开");
							return data;
						}
						if (rs != null) {
							while (rs.next()) {
								Map<String, Object> row = new LinkedHashMap<String, Object>();
								int n = rs.getMetaData().getColumnCount();
								for (int i = 0; i < n; i++) {
									row.put(rs.getMetaData().getColumnName(i + 1), rs.getObject(i + 1));
								}
								data.add(row);
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DataRetrievalFailureException("ERROR", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (cmt != null) {
					cmt.close();
					cmt = null;
				}
				if (conn != null && isClose) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();// 关闭异常
			}
		}
		return data;
	}


	public List<Map<String, Object>> executeWithResult(String procName, Map<String, Object> inParams,
			Map<String, Integer> outParams, Map<String, Object> inOutParams, ArrayList<String> procParams,
			String cursorName) throws DataAccessException {
		try {
			return executeWithResult(procName, jdbcTemplate.getDataSource().getConnection(), true, inParams, outParams,
					inOutParams, procParams, cursorName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> executeWithResult(String json) throws DataAccessException {
		if (logger.isDebugEnabled())
			logger.debug("传入json串：" + json);

		if (StringUtils.isBlank(json)) {
			logger.debug("传入json不能为空");
			return null;
		}

		Map<String, Object> m_json = JsonUtils.jsonToMap(json);
		String dbid = ObjectUtils.toString(m_json.get("dbid"));
		String procName = (String) m_json.get("procName");
		String cursorName = (String) m_json.get("cursorName");
		Object prefixIndex = m_json.get("prefixIndex");

		String inParams = StringUtils.substringBetween(json, "\"inParams\":", "}");
		if (StringUtils.endsWith(inParams, "}") == false)
			inParams += "}";

		String outParams = StringUtils.substringBetween(json, "\"outParams\":", "}");
		if (StringUtils.endsWith(outParams, "}") == false)
			outParams += "}";

		if (StringUtils.isBlank(dbid) || StringUtils.isBlank(procName)) {
			logger.debug("传入分站或过程名不能为空");
			return null;
		}

		Map<String, Object> m_inParams = JsonUtils.jsonToMap(inParams);
		Map m_outParams = JsonUtils.jsonToMap(outParams);
		

		if (MapUtils.isEmpty(m_inParams) && MapUtils.isEmpty(m_outParams)) {
			logger.debug("输入参数和输出参数不能都为空");
			return null;
		}
		// 设置数据源为分站
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_SUBDB_PREFIX + dbid);
		if (prefixIndex == null || "".equals(prefixIndex)) {
			return executeWithResult(procName, m_inParams, m_outParams, cursorName);
		} else {
			String[] prefixIndexs = String.valueOf(prefixIndex).split(",");
			return executeWithResult(procName, m_inParams, m_outParams, m_outParams,
					new ArrayList<String>(Arrays.asList(prefixIndexs)), cursorName);
		}
	}

	@Override
	public Long findForLong(String sql) throws DataAccessException {
		return jdbcTemplate.queryForObject(sql,Long.class);
	}

	@Override
	public int update(String sql, Map<String, Object> inParams) throws DataAccessException {
		return namedParameterJdbcTemplate.update(sql, inParams);
	}
	
	@Override
	public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
		return namedParameterJdbcTemplate.update(sql, paramSource);
	}
	
	public synchronized Map<String, Object> jdbcOne(Connection connection, String sql, boolean isCloseConnect)
			throws Exception {
		logger.info("SQL_:" + sql);
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		try {
			prepareStatement = connection.prepareStatement(sql);
			rs = prepareStatement.executeQuery();
			return JdbcHelper.extractData(rs);
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prepareStatement != null)
				try {
					prepareStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (connection != null)
				try {
					if (isCloseConnect) {
						connection.close();
					} else {
						logger.info("connection is not close ,as set isCloseConnect=" + isCloseConnect);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public synchronized Map<String, Object> jdbcOne(String sql) throws Exception {
		Connection connection = getJdbcTemplate().getDataSource().getConnection();
		return jdbcOne(connection, sql, true);
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	
	/**
	 * Setter注入datasource，同时获得Template
	 * 
	 * @param dataSource
	 */
	@Resource
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

    @Override
    public PreparedStatementResultSetHandle createPreparementResultSetHandle(String sql) throws Throwable {
        return BatchHandle.resultSet(getJdbcTemplate().getDataSource().getConnection(), sql);
    }
    
	public synchronized List<Map<String, Object>> jdbcList(Connection connection, String sql, boolean isClose)
			throws Exception {
		logger.info(sql);
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		try {
			connection = getJdbcTemplate().getDataSource().getConnection();
			prepareStatement = connection.prepareStatement(sql);
			rs = prepareStatement.executeQuery();
			return JdbcHelper.extractDatas(rs);
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prepareStatement != null)
				try {
					prepareStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (connection != null)
				try {
					if (isClose) {
						connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
    
	public synchronized List<Map<String, Object>> jdbcList(String sql) throws Exception {
		return jdbcList(getJdbcTemplate().getDataSource().getConnection(), sql, true);
	}
    
    /**
	 * 参数值写入sql内
	 * 
	 * @param sql
	 *            更新或插入语句
	 * @return
	 * @throws Exception
	 */
	public synchronized int jdbcUpdate(String sql) throws Exception {
		int i = jdbcUpdate(sql, null);
		return i;
	}

	/**
	 * 
	 * 问号代替实际值
	 * 
	 * @param sql
	 *            更新或插入语句
	 * 
	 * @param values
	 *            perparedstatment需要的参数值，顺序依据list.add
	 * @return
	 * @throws Exception
	 */
	public synchronized int jdbcUpdate(String sql, List<Object> values) throws Exception {
		logger.info("SQL:" + sql);
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		try {
			connection = getJdbcTemplate().getDataSource().getConnection();
			return jdbcUpdate(connection, sql, true, values);
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prepareStatement != null)
				try {
					prepareStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 
	 * 问号代替实际值
	 * 
	 * @param sql
	 *            更新或插入语句
	 * 
	 * @param values
	 *            perparedstatment需要的参数值，顺序依据list.add
	 * @return
	 * @throws Exception
	 */
	public synchronized int jdbcUpdate(Connection connection, String sql, boolean isCloseConnect, List<Object> values)
			throws Exception {
		logger.info(sql);
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		try {
			prepareStatement = connection.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				int size = values.size();
				for (int i = 0; i < size; i++) {
					prepareStatement.setObject(i + 1, values.get(i));
				}
			}
			return prepareStatement.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prepareStatement != null)
				try {
					prepareStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (connection != null)
				try {
					if (isCloseConnect) {
						connection.commit();
						connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public JdbcTemplate getMyJdbcTemplate() {
		return this.jdbcTemplate;
	}

	@Override
	public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) throws DataAccessException {
		return namedParameterJdbcTemplate.queryForObject(sql, paramSource, requiredType);
	}
	
	public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType)
			throws DataAccessException {
		return namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
	}
}