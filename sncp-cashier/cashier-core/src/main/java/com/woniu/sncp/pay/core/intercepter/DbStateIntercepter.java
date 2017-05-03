package com.woniu.sncp.pay.core.intercepter;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.woniu.sncp.pay.core.service.CoreDbService;

/**
 * <p>
 * descrption: 数据库状态检查拦截
 * </p>
 * 
 * @author fuzl
 * @date 2017年4月13日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Aspect
@Component
public class DbStateIntercepter {

	private static final Logger logger = LoggerFactory.getLogger(DbStateIntercepter.class);

	
	
	/** 
     * 定义拦截规则：拦截com.woniu.sncp.pay.core.service目录下的以及子目录下的所有类中，有create开头的方法。
     * execution(* com.woniu.sncp.pay.core.service.*Service.create*(..)) ||
     */
    @Pointcut("execution(* com.woniu.sncp.pay.core.service.*Service.create*(..)) || execution(* com.woniu.sncp.pay.core.service.*Service.update*(..)) || execution(* com.woniu.sncp.pay.core.service.*Service.query*(..)) "
    		+ "|| execution(* com.woniu.sncp.pay.core.service.*Impl.create*(..)) || execution(* com.woniu.sncp.pay.core.service.*Impl.update*(..)) || execution(* com.woniu.sncp.pay.core.service.*Impl.query*(..))"
    		+ "|| execution(* com.woniu.sncp.pay.core.service.m.*.create*(..)) || execution(* com.woniu.sncp.pay.core.service.m.*.query*(..))"
    		+ "|| execution(* com.woniu.sncp.pay.core.service.schedule.*.create*(..)) || execution(* com.woniu.sncp.pay.core.service.schedule.*.update*(..)) || execution(* com.woniu.sncp.pay.core.service.schedule.*.query*(..)) "
    		+ "|| execution(* com.woniu.sncp.pay.core.service.schedule.*.get*(..))")
	public void serviceAspect() {}

    @Autowired
    CoreDbService coreDbService;
    
    //是否开启检查db状态
    @Value(value = "${spring.db.open-check}")
    private String openCheck;
    
    @Value(value = "${spring.db.machine-name}")
    private String machineName;
    
    @Around("serviceAspect()")
    public Object around(ProceedingJoinPoint pjp){
    	Object obj = null;
    	try {
    		MethodSignature signature = (MethodSignature) pjp.getSignature();
        	Method method = signature.getMethod();
        	logger.debug(this.getClass().getSimpleName()+"--->拦截检测方法:{}",method.getName());
        	// 1. 确认是否需要检查
        	if(CoreDbService.DB_OPEN_CHECK.equals(openCheck)){
        		// 2. 查询当前db 主机状态
        		String dbState = coreDbService.getDbState(machineName);
        		
        		if(StringUtils.isNotBlank(dbState)){
        			if(!CoreDbService.DB_MACHINE_STATE_ONLINE.equals(dbState)){
    					//状态不在线,终止操作
    					logger.info("当前机器状态:{},{}",dbState,"请求终止");
    					return obj;
    				}
    				obj = pjp.proceed();
        		}else{
        			//获取不到db机器状态,终止操作
    				return obj;
        		}
        	}else{
        		// 不需要检查
        		obj = pjp.proceed();
        	}
		} catch (Exception e) {
			//获取db机器状态异常,终止操作
			logger.error(this.getClass().getSimpleName()+"------>检测db状态异常:{}",e);
		} catch (Throwable e) {
			logger.error(this.getClass().getSimpleName()+"------>系统异常:{}",e);
		}
		return obj;
    }
    
}
