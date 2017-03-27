package com.woniu.sncp.pay.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


public class JdbcHelper {
	public static Map<String, Object> extractData(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int num = md.getColumnCount();
		if (rs.next()) {
			Map<String, Object> mapOfColValues = new HashMap<String, Object>(num);
			for (int i = 1; i <= num; i++) {
				mapOfColValues.put(md.getColumnName(i), rs.getObject(i));
			}
			return mapOfColValues;
		}
		return null;
	}

	public static List<Map<String, Object>> extractDatas(ResultSet rs) throws SQLException {
		if (rs == null) {
			return null;
		}
		ResultSetMetaData md = rs.getMetaData();
		int num = md.getColumnCount();
		List<Map<String, Object>> listOfRows = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			Map<String, Object> mapOfColValues = new HashMap<String, Object>(num);
			for (int i = 1; i <= num; i++) {
				mapOfColValues.put(md.getColumnName(i), rs.getObject(i));
			}
			listOfRows.add(mapOfColValues);
		}
		return listOfRows;
	}

	public static int translateType(Object o) {
		if (o instanceof String) {
			return java.sql.Types.VARCHAR;
		} else if (o instanceof Integer) {
			return java.sql.Types.INTEGER;
		} else if (o instanceof Long) {
			return java.sql.Types.BIGINT;
		} else if (o instanceof Date) {
			return java.sql.Types.DATE;
		}
		return java.sql.Types.VARCHAR;
	}

	public static String jdbcMergeSql(String mergeInto, String using, String on, String matched, String unMatched) {
		if (StringUtils.isBlank(mergeInto) || StringUtils.isBlank(using) || StringUtils.isBlank(on)) {
			throw new IllegalArgumentException("mergeInfo/using/on 不应为空");
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(" merge into ").append(mergeInto);
		buffer.append(" using ").append(using);
		buffer.append(" on ").append(" (" + on + ") ");
		if (StringUtils.isNotBlank(matched) || StringUtils.isNotBlank(unMatched)) {
			if (StringUtils.isNotBlank(matched)) {
				buffer.append(" when matched then ");
				buffer.append("  ").append(matched);
			}
			if (StringUtils.isNotBlank(unMatched)) {
				buffer.append(" when not matched then ");
				buffer.append("  ").append(unMatched);
			}
		} else {
			throw new IllegalArgumentException("匹配 或 未能匹配 至少存在一个");
		}
		return buffer.toString();
	}
}