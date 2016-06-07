package com.woniu.sncp.nciic.oracle;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONObject;

@Configuration
@ConfigurationProperties("oracle")
public class OracleDatasourceConfiguration {

	private String druidconf = "";

	public void setDruidconf(String druidconf) {
		this.druidconf = druidconf;
	}
	
	@Bean
	public DataSource initDruidDatasource()
			throws Exception {
		return DruidDataSourceFactory.createDataSource(JSONObject.parseObject(druidconf));
	}

	@Bean
	public JdbcTemplate jdbcTemplate() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(initDruidDatasource());
		return jdbcTemplate;
	}
}
