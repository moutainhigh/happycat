package com.woniu.sncp.pay.core.service;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.Payment;
import com.woniu.sncp.pay.core.transfer.platform.AbstractTransfer;


/**
 * 支付抽象平台服务类 
 * 
 * @author luzz
 *
 */
@Service("paymentService")
public class PaymentServiceImpl implements PaymentService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, AbstractPayment> paymentMap;
    
    @Autowired
    private Map<String, AbstractTransfer> transferMap;

    @Override
    public long findPaymentIdByPayment(Payment actualImprestPayment) {
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
    public AbstractPayment findPaymentById(long paymentId) throws IllegalArgumentException {
        Assert.assertNotSame("支付平台ID不合法", 0L, paymentId);
        AbstractPayment object = paymentMap.get("PAYMENT_" + paymentId);
        if (object == null) {
			return paymentMap.get("PAYMENT_9999");// 默认远程调用
		}
        return object;
    }

	@Override
	public AbstractTransfer findTransferById(long paymentId)
			throws IllegalArgumentException {
		Assert.assertNotSame("支付平台ID不合法", 0L, paymentId);
        return transferMap.get("TRANSFER_" + paymentId);
	}

}

