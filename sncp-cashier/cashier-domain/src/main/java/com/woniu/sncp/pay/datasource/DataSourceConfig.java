package com.woniu.sncp.pay.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>descrption: 数据源配置</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
public class DataSourceConfig {

    /**
     * 收银台默认使用此数据源
     * @return
     */
    @Primary
    @Bean(name = "dataSource")
    @Qualifier("dataSource")
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    /**
//     * 队列使用数据源
//     * @return
//     */
//    @Bean(name = "queueDataSource")
//    @Qualifier("queueDataSource")
//    @ConfigurationProperties(prefix="spring.queue-datasource")
//    public DataSource queueDataSource() {
//        return DataSourceBuilder.create().build();
//    }
    
    @Primary
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(
            @Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

//    @Bean(name = "queueJdbcTemplate")
//    public JdbcTemplate queueJdbcTemplate(
//            @Qualifier("queueDataSource") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
    
    
}