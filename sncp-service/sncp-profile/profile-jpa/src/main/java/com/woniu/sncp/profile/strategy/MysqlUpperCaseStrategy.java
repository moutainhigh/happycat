package com.woniu.sncp.profile.strategy;

import org.hibernate.cfg.ImprovedNamingStrategy;

public class MysqlUpperCaseStrategy extends ImprovedNamingStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4891019622914690807L;

	@Override
	public String tableName(String tableName) {
		return tableName.toUpperCase();
	}
}
