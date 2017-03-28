package com.woniu.sncp.pay.core.service;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.common.exception.CallBackException;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.SubmitRequestException;
import com.woniu.sncp.pay.common.threadpool.ThreadPool;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageTask;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.Payment;

/**
 * 
 * <p>descrption: 退款抽象平台服务类 实现</p>
 * 
 * @author fuzl
 * @date   2015年10月9日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("refundmentService")
public class RefundmentServiceImpl implements RefundmentService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private ThreadPool threadPool;
	
	@Resource
    private Map<String, AbstractPayment> paymentMap;

    @Override
    public long findRefundmentIdByPayment(Payment actualImprestPayment) {
        Iterator<String> iter = paymentMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object obj = paymentMap.get(key);
            if (obj.getClass().getName().equals(actualImprestPayment.getClass().getName()))
                return NumberUtils.toLong(StringUtils.substringAfterLast(key, "PAYMENT_"));
        }
        return 0;
    }

    @Override
    public AbstractPayment findPaymentById(long refundmentId) throws IllegalArgumentException {
        Assert.assertNotSame("退款平台ID不合法", 0L, refundmentId);
        return paymentMap.get("PAYMENT_" + refundmentId);
    }
    
    
    public void monitorExcetpionToAlter(Exception exception) {
		if (exception instanceof SubmitRequestException) {
			logger.info(exception.getMessage());
			threadPool.executeTask(new MonitorMessageTask(exception.getMessage(), "1"));
		} else if (exception instanceof CallBackException) {
			logger.info(exception.getMessage());
			threadPool.executeTask(new MonitorMessageTask(exception.getMessage(), "1"));
		} else if (exception instanceof DataAccessException) {
			logger.info(exception.getMessage());
			threadPool.executeTask(new MonitorMessageTask(exception.getMessage(), "1"));
		} else if (exception instanceof IllegalArgumentException) {
			logger.info(exception.getMessage());
			threadPool.executeTask(new MonitorMessageTask(exception.getMessage(),"1"));
		} else if (exception instanceof OrderIsSuccessException) {

		} else {
			logger.info(exception.getMessage());
			threadPool.executeTask(new MonitorMessageTask(exception.getMessage(), "1"));
		}
	}
}

