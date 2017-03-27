package com.woniu.sncp.pay.datasource;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>descrption: 账号相关数据源配置-JPA</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="entityManagerFactoryPassport",
        transactionManagerRef="transactionManagerPassport",
        basePackages= { "com.woniu.sncp.pay.repository.passport" }) //设置Repository所在位置
public class PassportJpaConfig {

	@Autowired 
	@Qualifier("passportDataSource")
    private DataSource passportDataSource;

    @Bean(name = "entityManagerPassport")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryPassport(builder).getObject().createEntityManager();
    }

    @Bean(name = "entityManagerFactoryPassport")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryPassport (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(passportDataSource)
                .properties(getVendorProperties(passportDataSource))
                .packages("com.woniu.sncp.pay.repository.passport") //设置实体类所在位置
                .persistenceUnit("passportPersistenceUnit")
                .build();
    }

    @Autowired
    private JpaProperties jpaProperties;

    private Map<String, String> getVendorProperties(DataSource dataSource) {
        return jpaProperties.getHibernateProperties(dataSource);
    }

    @Bean(name = "transactionManagerPassport")
    PlatformTransactionManager transactionManagerPassport(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryPassport(builder).getObject());
    }
}
