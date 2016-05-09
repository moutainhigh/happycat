package com.woniu.sncp.fcm.mongo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

@Configuration
@EnableMongoRepositories(basePackages="com.woniu.sncp.fcm.mongo.repository")
public class MongoConfig {
	
	@Value("${mongodb.ip}")
	private String mongodbIp;//ip
	
	@Value("${mongodb.port}")
	private int mongodbPort;//端口
	
	@Value("${mongodb.collectionName}")
	private String collectionName;//表名
	
	@Value("${mongodb.userName}")
	private String userName;//用户名
	
	@Value("${mongodb.userPwd}")
	private String userPassword;//密码
	
	@Value("${mongodb.connection.timeout}")
	private int connectionTimeout;//连接超时时间 毫秒
	
	@Value("${mongodb.connectionsPerHost}")
	private int connectionsPerHost;//连接数
	
	public @Bean MongoDbFactory mongoDbFactory() throws Exception {
		
		String strUri = String.format("mongodb://%s:%d/%s", mongodbIp, mongodbPort, collectionName);
		WriteConcern writeConcern = WriteConcern.JOURNAL_SAFE;
		MongoClientOptions.Builder  builder= MongoClientOptions
												.builder()
												.connectTimeout(connectionTimeout)
												.connectionsPerHost(connectionsPerHost)
												.writeConcern(writeConcern);
		MongoClientURI mongoUri = new MongoClientURI(strUri,builder);

		return new SimpleMongoDbFactory(mongoUri);
	}

	public @Bean MongoTemplate mongoTemplate() throws Exception {
		
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
				
		return mongoTemplate;
		
	}
}
