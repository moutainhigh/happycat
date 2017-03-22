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
 * <p>descrption: 队列数据源配置-JPA</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="entityManagerFactoryQueue",
        transactionManagerRef="transactionManagerQueue",
        basePackages= { "com.woniu.sncp.pay.repository.queue" }) //设置Repository所在位置
public class QueueJpaConfig {

	@Autowired 
	@Qualifier("queueDataSource")
    private DataSource queueDataSource;

    @Bean(name = "entityManagerQueue")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryQueue(builder).getObject().createEntityManager();
    }

    @Bean(name = "entityManagerFactoryQueue")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryQueue (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(queueDataSource)
                .properties(getVendorProperties(queueDataSource))
                .packages("com.woniu.sncp.pay.repository.queue") //设置实体类所在位置
                .persistenceUnit("queuePersistenceUnit")
                .build();
    }

    @Autowired
    private JpaProperties jpaProperties;

    private Map<String, String> getVendorProperties(DataSource dataSource) {
        return jpaProperties.getHibernateProperties(dataSource);
    }

    @Bean(name = "transactionManagerQueue")
    PlatformTransactionManager transactionManagerQueue(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryQueue(builder).getObject());
    }
}
