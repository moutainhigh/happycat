package com.woniu.sncp.pay.core.service.payment.platform.shenzpay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerConnSzxUtils {

	protected static final Logger logger = LoggerFactory.getLogger(ServerConnSzxUtils.class);
    /**
     * 对卡信息进行des加密，并进行base64编码
     *
     * @param cardMoney 卡金额
     * @param cardSn    卡序列号
     * @param cardPwd   卡密码
     * @param desKey    des密码
     * @return 进行des加密，并进行base64的字符串
     */
    public static String getDesEncryptBase64String(String cardMoney, String cardSn, String cardPwd, String desKey) {
    	logger.info("DES String:" + cardMoney + "@" + cardSn + "@" + cardPwd);
        //String desString = DesEncrypt.getEncString(cardMoney + "@" + cardSn + "@" + cardPwd, desKey, "UTF8");
        String desString;
        try {
            desString = DES.encode(cardMoney + "@" + cardSn + "@" + cardPwd, desKey);
        } catch (Exception e) {
            e.printStackTrace();
            desString = "";
        }
        return desString;
    }
    
    /**
     * 对卡信息进行des加密，并进行base64编码
     *
     * @param cardMoney 卡金额
     * @param cardSn    卡序列号
     * @param cardPwd   卡密码
     * @param desKey    des密码
     * @return 进行des加密，并进行base64的字符串
     */
    public static String getDesEncryptBase64String(String cardType ,String cardMoney, String cardSn, String cardPwd, String desKey) {
    	logger.info("DES String:" + cardType + "@" + cardMoney + "@" + cardSn + "@" + cardPwd);
        //String desString = DesEncrypt.getEncString(cardMoney + "@" + cardSn + "@" + cardPwd, desKey, "UTF8");
        String desString;
        try {
            desString = DES.encode(cardType + "@" +cardMoney + "@" + cardSn + "@" + cardPwd, desKey);
        } catch (Exception e) {
            e.printStackTrace();
            desString = "";
        }
        return desString;
    }

}
