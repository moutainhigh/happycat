package com.woniu.sncp.pay.core.service.payment.platform.cmpay.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author hisun
 * @version version2.0.0
 * 
 * */
public class HiiposmUtil {
	/**
	 * 
	 * MD5签名验证.
	 * 
	 * @param srcData
	 *            MD5签名原报文，hmac 消息摘要，key签名密钥
	 * @return 验证结果：true 验签成功；false 验签失败
	 * 
	 * */

	public boolean MD5Verify(String srcData, String hmac, String key) {
		String _crpy = MD5Sign(srcData, key);
		return _crpy.equals(hmac);
	}

	/**
	 * 
	 * 商户与手机支付平台http会话的发送与接收.<br/>
	 * 商户向手机支付平台发送http请求，同时接收手机支付平台对此请求的响应
	 * 
	 * @param url
	 *            商户请求的路由地址，buf 请求的参数
	 * @return reqMsg 请求的响应报文
	 * @exception IOException
	 *                抛出IO异常
	 * 
	 * */

	public String sendAndRecv(String url, String buf, String characterSet) throws IOException {
		String charType;
		if("00".equals(characterSet)){
			charType = "GBK";
		} else if("01".equals(characterSet)){
			charType = "GB2312";
		} else if("02".equals(characterSet)){
			charType = "UTF-8";
		} else {
			charType = null;
		}
		
		HttpClientParams params = new HttpClientParams();
		params.setContentCharset(charType);
		HttpClient hc = new HttpClient();
		params.setSoTimeout(120000);
		hc.setParams(params);
		PostMethod pm = new PostMethod(url);
		pm.addRequestHeader("Content-Type",
				"application/x-www-form-urlencoded;charset="+charType);
		String[] aParam = buf.split("&");
		if (aParam.length == 0) {
			return null;
		}
		int z = 0;
		for (int i = 0; i < aParam.length; ++i) {
			z = aParam[i].indexOf('=');
			if (z != -1) {
				pm.addParameter(aParam[i].substring(0, z++), aParam[i]
						.substring(z));
			}

		}

		String repMsg = "";
		try {
			hc.executeMethod(pm);
			repMsg = pm.getResponseBodyAsString();
		} finally {
			pm.releaseConnection();
			pm = null;
			hc = null;
		}

		return repMsg;
	}

	/**
	 * 
	 * MD5数据签名.
	 * 
	 * @param signData
	 *            签名原报文，signkey 签名密钥
	 * @return 签名后的消息摘要
	 * 
	 * */

	public String MD5Sign(String signData, String signkey) {
		CryptUtilImpl impl = new CryptUtilImpl();
		String value = impl.cryptMd5(signData, "");
		String value2 = impl.cryptMd5(value, signkey);

		return value2;
	}

	/**
	 * 
	 * 以"键值对名称"获取响应报文中"键值对值".
	 * 
	 * @param respMsg
	 *            响应报文，name 键值对名称
	 * @return 键值对值
	 * 
	 * */

	public String getValue(String respMsg, String name) {
		String[] resArr = StringUtils.split(respMsg, "&");

		Map resMap = new HashMap();
		for (int i = 0; i < resArr.length; ++i) {
			String data = resArr[i];
			int index = StringUtils.indexOf(data, '=');
			String nm = StringUtils.substring(data, 0, index);
			String val = StringUtils.substring(data, index + 1);
			resMap.put(nm, val);
		}
		return ((String) resMap.get(name) == null) ? "" : (String) resMap
				.get(name);
	}

	/**
	 * 
	 * 处理手机支付平台返回的支付url.<br/>
	 * 手机支付平台返回的url是特殊处理的格式，如下：<br/>
	 * url&lt;hi:=&gt;https://ipos.10086.cn/ips/FormTrans3&lt;hi:$$&gt;method&lt
	 * ;hi:=&gt;post&lt;hi:$$&gt;sessionId&lt;hi:=&gt;2011010199999 <br/>
	 * 处理后：https://ipos.10086.cn/ips/FormTrans3?sessionId=2011010199999
	 * 
	 * @param payUrl
	 *            手机支付返回的支付url
	 * @return 处理后的url
	 * 
	 * */

	public static String getRedirectUrl(String payUrl) {

		HashMap rdUrl = new HashMap();
		if (payUrl != null) {
			String[] items = payUrl.split("[<hi:$$>]{7}");
			if (items != null) {
				for (int i = 0; i < items.length; i++) {
					String item = (String) items[i];
					if (item != null) {
						String[] element = item.split("[<hi:=>]{6}");
						if (element != null && element.length == 2) {
							rdUrl.put(element[0], element[1]);
						}
					}
				}
			}
		}
		return rdUrl.get("url") + "?" + "sessionId=" + rdUrl.get("sessionId");
	}

	/*public static void main(String[] args) {
		String source = "888073174280001MCG00000SUCCESSMD5page_notify2.0.010CNY_AMT=#CMY_AMT=#RED_AMT=#VCH_AMT=#POT_CHG_AMT=0****20110115111801SUCCESS";
		String key = "1j1F0lzfgJjFEwYk0UsryI66Z0r4lbneyE9M8qf6IB1qBfo02bWGm1EuBGKhCCTz";
        HiiposmUtil util = new HiiposmUtil();
		String md5 = util.MD5Sign(source, key);
		System.out.println(md5);
	}*/
}
