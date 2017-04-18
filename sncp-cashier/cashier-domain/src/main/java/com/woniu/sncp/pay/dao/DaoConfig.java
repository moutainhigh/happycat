package com.woniu.sncp.pay.dao;

import java.io.Serializable;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.woniu.sncp.dao.BaseHibernateDAO;
import com.woniu.sncp.dao.HibernateDaoImpl;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.passport.PassportAsyncTask;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.queue.PassportQueue;

/**
 * <p>
 * descrption:
 * </p>
 * 
 * @author fuzl
 * @date 2017年3月20日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
//@Configuration
public class DaoConfig{

	@Autowired  
	Environment env; 

	// sessionFactory
	@Bean
	public LocalSessionFactoryBean sessionFactory() throws Exception {
		LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
		localSessionFactoryBean.setDataSource(dataSource);
		Properties properties1 = new Properties();
		properties1.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		properties1.setProperty("hibernate.show_sql", "false");
		localSessionFactoryBean.setHibernateProperties(properties1);
		localSessionFactoryBean.setPackagesToScan("*");
		return localSessionFactoryBean;
	}

	// 创建事务 DataSource spring会自动导入到参数
	@Bean
	public DataSourceTransactionManager transaction(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Autowired
	public DataSource dataSource;
//	@Bean
//	public DataSource dataSource() throws Exception {
//
//		String driverClassName = env.getProperty("driverClassName").toString();
//		String url = env.getProperty("url").toString();
//		String username = env.getProperty("username").toString();
//		String password = env.getProperty("password").toString();
//
//		DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
//				.username(username).password(password);
//		return factory.build();
//	}

	@Bean(name = { "ppQueueTaskDao" })
	public BaseHibernateDAO<PassportQueue, Serializable> getPpQueueTaskDao() {
		return new HibernateDaoImpl<PassportQueue, Serializable>();
	};

	@Bean(name = { "ppAsyncTaskDao" })
	public BaseHibernateDAO<PassportAsyncTask, Serializable> getPpAsyncTaskDao() {
		return new HibernateDaoImpl<PassportAsyncTask, Serializable>();
	};

	@Bean(name = { "paymentOrderDao" })
	public BaseHibernateDAO<PaymentOrder, Serializable> getPaymentOrderDao() {
		return new HibernateDaoImpl<PaymentOrder, Serializable>();
	};

	@Bean(name = { "passportDao" })
	public BaseHibernateDAO<Passport, Serializable> getPassportDao() {
		return new HibernateDaoImpl<Passport, Serializable>();
	}

	// @Bean(name={"hibernateDao"})
	// public BaseHibernateDAO<E, Serializable> getHibernateDao(){
	// return new HibernateDaoImpl<E, Serializable>();
	// };

}
