package com.woniu.sncp.pay.core.service.payment.platform.alipay;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * <pre>
 * 支付宝网银直接支付(专用纯网关) - 公网环境，无测试环境 - DSA加密（为方便，目前使用证书和账号支付相同）
 * 1.加密串是按照key的<font color=red>升序排列</font>，DSA加密，无需额外增加MD5加密串去校验
 * 2.提交订单时比支付宝账号支付多2个参数
 * 3.纯网关支付的账号(snail.account@snailgame.net)和账号支付的帐号(snail.account2@snailgame.net)不同！
 * </pre>
 *
 *
 */
@Service("alipayDPPayment")
public class AlipayDPPayment extends AlipayPayment{

    @Override
    public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
        // 银行直连参数校验
        String defaultbank = (String)inParams.get("defaultbank");
        if (StringUtils.isBlank(defaultbank)){
            throw new ValidationException("银行直连参数缺少支付银行参数");
        }

        // 1.拼装参数
        PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
        Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

        String priKey = AlipayHelper.readText(platform.getPrivateUrl());

        HashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("service", "create_direct_pay_by_user");
        params.put("partner", platform.getMerchantNo());
        params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
        params.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
        params.put("_input_charset", _charset_encode);
        params.put("subject", StringUtils.trim((String) inParams.get("productName")));
        params.put("body", StringUtils.trim((String) inParams.get("productName")));
        params.put("out_trade_no", paymentOrder.getOrderNo());
        params.put("total_fee", ObjectUtils.toString(paymentOrder.getMoney()));
        params.put("payment_type", "1"); // 固定值
        params.put("paymethod", "bankPay"); // 网银 - 比帐号支付多的参数
        params.put("defaultbank", inParams.get("defaultbank")); // 默认支付网银(前台获取) - 比帐号支付多的参数
        params.put("seller_email", platform.getManageUser()); // 支付宝后台我方的帐号

        LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);

        String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);

        // 2.加密
        Map<String, Object> encryptParams = new HashMap<String, Object>();
        encryptParams.put("source", source);
        encryptParams.put("priKey", priKey);
        String sign = this.encode(encryptParams);

        // 3.剩余需要传递参数
        params.put("sign_type", "DSA");
        params.put("sign", sign);
        params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
        params.put("acceptCharset", _charset_encode); // 提交给对方的支付编码

        return params;
    }

}
