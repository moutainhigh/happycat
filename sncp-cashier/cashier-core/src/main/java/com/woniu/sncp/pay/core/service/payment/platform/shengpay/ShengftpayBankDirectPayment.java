package com.woniu.sncp.pay.core.service.payment.platform.shengpay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service("shengpayBankDirectPayment")
public class ShengftpayBankDirectPayment extends ShengftpayPayment{
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		
		// 1.银行直连参数校验
        String defaultbank = (String)inParams.get("defaultbank");
        if (StringUtils.isBlank(defaultbank)){
            throw new ValidationException("银行直连参数缺少支付银行参数");
        }
		
		// 2.获取参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Name", "B2CPayment");
		params.put("Version", "V4.1.1.1.1");
		params.put("Charset", "UTF-8");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		params.put("MsgSender", platform.getMerchantNo());//商户号
		params.put("SendTime", sdf.format(new Date()));
		params.put("OrderNo", paymentOrder.getOrderNo());
		params.put("OrderAmount", String.valueOf(paymentOrder.getMoney()));
		params.put("OrderTime", sdf.format(paymentOrder.getCreateDate()));
		params.put("PayType", "PT001");//支付类型: PT018 盛大一卡通,PT020 娱乐一卡通
		String backCode = String.valueOf(inParams.get("defaultbank"));
		String bankCardType = String.valueOf(inParams.get("bankCardType"));//收银台传递的银行卡类型,0储蓄 1信用
//		String payType = "19";
//		if("1".equals(bankCardType)){//判断是否为信用卡,默认储蓄卡
//			payType = "20";
//		}
//		if(StringUtils.isNotBlank(backCode)){
//			String[] items = backCdPayType.split("_");
//			backCd = items[0];
//			if(items.length == 2){
//				if("D".equalsIgnoreCase(items[1])){
//					payType = "19";
//				}else if("C".equalsIgnoreCase(items[1])){
//					payType = "20";
//				}
//			} 
//		}
		String payType = "04";//改为04 综合网银
		params.put("PayChannel", payType);//支付渠道 19 储蓄卡，20 信用卡 
		params.put("InstCode", backCode);//银行编码
		params.put("PageUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));//支付成功后客户端浏览器回调地址
		params.put("NotifyUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));//服务端通知发货地址
		params.put("ProductName", StringUtils.trim((String) inParams.get("productName")));
		params.put("BuyerIp", inParams.get("clientIp"));//ip
		params.put("SignType", "MD5");
		
		// 2.加密
		StringBuffer source = new StringBuffer();//加密所需的字符串
		source.append(params.get("Name"));
		source.append(params.get("Version"));
		source.append(params.get("Charset"));
		source.append(params.get("MsgSender"));
		source.append(params.get("SendTime"));
		source.append(params.get("OrderNo"));
		source.append(params.get("OrderAmount"));
		source.append(params.get("OrderTime"));
		source.append(params.get("PayType"));
		source.append(params.get("PayChannel"));
		source.append(params.get("InstCode"));
		source.append(params.get("PageUrl"));
		source.append(params.get("NotifyUrl"));
		source.append(params.get("ProductName"));
		source.append(params.get("BuyerIp"));
		source.append(params.get("SignType"));
		source.append(platform.getPayKey());
		
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source.toString());
		
		String sign =encode(encryptParams);// 数字签名（32位的md5加密,加密后转换成大写）
		
		params.put("SignMsg", sign);
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.put("urlcode", "UTF-8"); // 提交给对方的支付编码
		
		return params;
	}
}
