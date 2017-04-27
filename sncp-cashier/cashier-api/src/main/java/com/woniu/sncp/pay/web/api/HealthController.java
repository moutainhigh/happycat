package com.woniu.sncp.pay.web.api;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woniu.sncp.pay.core.service.CoreDbService;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年4月14日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@RestController
public class HealthController implements HealthIndicator{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	@Autowired
    CoreDbService coreDbService;
	
	//是否开启检查db状态
    @Value(value = "${spring.db.open-check}")
    private String openCheck;
    
    @Value(value = "${spring.db.machine-name}")
    private String machineName;
	
    /**
     * 应用健康检查入口
     * @return
     */
    @RequestMapping("/check/health")
	public Boolean isHealth(){
		return true;
	}
    
    /**
     * 查询数据库机器是否正常和数据库是否正常
     * @return
     */
    @RequestMapping("/check/active")
	public Boolean isActive(HttpServletResponse response){
		try {
			// 1. 确认是否需要检查
        	if(CoreDbService.DB_OPEN_CHECK.equals(openCheck)){
        		// 2. 查询当前db 主机状态
        		String dbState = coreDbService.getDbState(machineName);
        		if(StringUtils.isNotBlank(dbState)){
        			if(!CoreDbService.DB_MACHINE_STATE_ONLINE.equals(dbState)){
    					//状态不在线,终止操作
    					logger.info("当前机器状态:{},{}",dbState,"请求终止");
    					return false;
    				}
        		}else{
        			//获取不到db机器状态,终止操作
    				return false;
        		}
        	}
        	
        	// 3.查询测试sql
			String queryResult = coreDbService.getQueryTestResult();
			if(queryResult.endsWith("1")){
				return true;
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
			response.setStatus(500);
		}
		return false;
	}

	@Override
	public Health health() {
		Builder down = Health.down();
		try {
			// 1. 确认是否需要检查
        	if(CoreDbService.DB_OPEN_CHECK.equals(openCheck)){
        		// 2. 查询当前db 主机状态
        		String dbState = coreDbService.getDbState(machineName);
        		if(StringUtils.isNotBlank(dbState)){
        			if(!CoreDbService.DB_MACHINE_STATE_ONLINE.equals(dbState)){
    					//状态不在线,终止操作
    					logger.info("当前机器状态:{},{}",dbState,"请求终止");
    					return down.build();
    				}
        		}else{
        			//获取不到db机器状态,终止操作
        			return down.build();
        		}
        	}
        	
        	// 3.查询测试sql
			String queryResult = coreDbService.getQueryTestResult();
			if(queryResult.endsWith("1")){
				return Health.up().build();
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
			return down.build();
		}
		return Health.up().build();
	}
}
