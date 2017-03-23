package com.woniu.sncp.pay.core.service.payment.platform.alipay.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.utils.encrypt.Dsa;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.security.signature.DSASignature;

public class AlipayHelper {
	private static Logger logger = LoggerFactory.getLogger(AlipayHelper.class);

    private AlipayHelper(){}
	/***
	 * DSA签名(单笔订单对账)
	 * 
	 * @param service
	 *            快速付款交易服务
	 * @param sign_type
	 *            签名方式
	 * @param out_trade_no
	 *            商户网站订单
	 * @param input_charset
	 *            字符编码
	 * @param partner
	 *            支付宝合作伙伴id
	 * @param priKey
	 *            DSA私钥
	 * @return
	 */
	public static String dsaQuerySign(String service, String sign_type, String out_trade_no, String input_charset,
			String partner, String priKey) {
		Properties properties = new Properties();
		properties.put("service", service);
		properties.put("out_trade_no", out_trade_no);
		properties.put("partner", partner);
		properties.put("_input_charset", input_charset);
		String sign = null;
		DSASignature dsaSignature = new DSASignature();
		try {
			sign = dsaSignature.sign(getSignatureContent(properties), priKey, input_charset);
		} catch (Exception e) {
			logger.error("", e);
		}
		return sign;
	}

	/***
	 * 直接生成URL
	 * 
	 * @param paygateway
	 * @param service
	 * @param sign_type
	 * @param out_trade_no
	 * @param input_charset
	 * @param partner
	 * @param priKey
	 * @return
	 */
	public static String dsaQuerySign(String paygateway, String service, String sign_type, String out_trade_no,
			String input_charset, String partner, String priKey) {
		Properties properties = new Properties();
		properties.put("service", service);
		properties.put("out_trade_no", out_trade_no);
		properties.put("partner", partner);
		properties.put("_input_charset", input_charset);
		String sign = null;
		try {
			DSASignature dsaSignature = new DSASignature();
			sign = dsaSignature.sign(getSignatureContent(properties), priKey, input_charset);
			logger.info("Snail check order sign: " + sign);
		} catch (Exception e) {
			logger.error("", e);
		}
		String url = null;
		try {
			url = paygateway + getURLContent(properties, sign, input_charset, sign_type);
		} catch (Exception e) {
			logger.error("", e);
		}
		return url;
	}

	public static String rsaQuerySign(String paygateway, String service, String sign_type, String out_trade_no,
			String input_charset, String partner, String priKey) {
		Properties properties = new Properties();
		properties.put("service", service);
		properties.put("out_trade_no", out_trade_no);
		properties.put("partner", partner);
		properties.put("_input_charset", input_charset);
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(getSignatureContent(properties), priKey,"");
			logger.info("Snail check order sign: " + sign);
		} catch (Exception e) {
			logger.error("", e);
		}
		String url = null;
		try {
			url = paygateway + getURLContent(properties, sign, input_charset, sign_type);
		} catch (Exception e) {
			logger.error("", e);
		}
		return url;
	}

	/***
	 * DSA校验
	 * 
	 * @param properties
	 *            post属性集合
	 * @param pubKey
	 *            公钥
	 * @param inputCharset
	 *            字符编码
	 * @param signature
	 *            签名
	 * @return
	 * @throws Exception
	 */
	public static boolean dsaCheck(Properties properties, String pubKey, String inputCharset, String signature) {
		String contents = getSignatureContent(properties);
		boolean ret = false;
		try {
			ret = EncryptFactory.getInstance(Dsa.NAME).verify(signature, contents,  pubKey, inputCharset);
		} catch (Exception e) {
			logger.error("", e);
		}
		if (!ret) {
			logger.debug("支付宝加密源串:" + contents);
		}
		return ret;
	}
	/***
	 * 支付宝通知验证
	 * 
	 * @param urlStr
	 *            验证URL
	 * @return
	 */
	public static String checkURL(String urlStr) {
		String ret = "";
		BufferedReader in = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			ret = in.readLine();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return ret;
	}

    /**
     * 发送请求，并返回响应
     * @param urlStr
     * @return
     */
    public static String httpsRequest(String urlStr) {
        StringBuilder ret = new StringBuilder();
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                ret.append(line);
            }
        } catch (Exception e) {
            logger.error("Https 请求失败", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return ret.toString();
    }
	/**
	 * 参数排序
	 * 
	 * @param properties
	 *            支付参数
	 * @return 排序后字符串
	 */
	@SuppressWarnings("unchecked")
	public static String getSignatureContent(Properties properties) {
		StringBuffer content = new StringBuffer();
		List keys = new ArrayList(properties.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = properties.getProperty(key);
			if (key.equalsIgnoreCase("id") || value == null || value.trim().length() == 0) {
				continue;
			}
			content.append((i == 0 ? "" : "&") + key + "=" + value);
		}
		return content.toString();
	}

	/**
	 * 将map升序排列
	 * 
	 * @param params
	 * @return
	 */
	public static LinkedHashMap<String, Object> sortMap(Map<String, Object> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		// 升序排列
		Collections.sort(keys);
		LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
		for (String key : keys) {
			linkedHashMap.put(key, params.get(key));
		}
		return linkedHashMap;
	}

	@SuppressWarnings("unchecked")
	private static String getURLContent(Properties properties, String sign, String inputCharset, String signType)
			throws Exception {
		StringBuffer parameter = new StringBuffer();
		List keys = new ArrayList(properties.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = URLEncoder.encode(properties.getProperty(key), inputCharset);

			parameter.append((i == 0 ? "" : "&") + key + "=" + value);
		}
		return parameter.toString() + "&sign=" + URLEncoder.encode(sign, inputCharset) + "&sign_type=" + signType;
	}
	
	public static String getURLContent(Map<String,Object> params, String sign, String inputCharset, String signType)
			throws Exception {
		StringBuffer parameter = new StringBuffer();
		List keys = new ArrayList(params.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = URLEncoder.encode(String.valueOf(params.get(key)), inputCharset);

			parameter.append((i == 0 ? "" : "&") + key + "=" + value);
		}
		return parameter.toString() + "&sign=" + URLEncoder.encode(sign, inputCharset) + "&sign_type=" + signType;
	}

	/**
	 * 读取加密key的内容
	 * 
	 * @param file
	 * @return
	 */
	public static String readText(String file) {
		String ret = "";
		try {
			ret = IOUtils.toString(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
		return ret;
	}

	public static boolean rsaCheck(Properties properties, String pubKey, String _charset_encode, String signature) {
		String contents = getSignatureContent(properties);
		boolean ret = false;
		try {
			ret = EncryptFactory.getInstance(Rsa.NAME).verify(signature, contents, pubKey, _charset_encode);
		} catch (Exception e) {
			logger.error("", e);
		}
		if (!ret) {
			logger.debug("支付宝加密源串:" + contents);
		}
		return ret;
	}

    /**
     * MD5 check
     * 主要是为了测试，最终使用DSA或RSA
     * @param properties
     * @param _charset_encode
     * @param signature
     * @return
     */
    public static boolean md5Check(Properties properties,String md5Key,String _charset_encode, String signature) {
        String contents = getSignatureContent(properties);
        boolean ret = false;
        try {
            ret = md5Encrypt(contents+md5Key,_charset_encode).equalsIgnoreCase(signature);
        } catch (Exception e) {
            logger.error("", e);
        }
        if (!ret) {
            logger.debug("支付宝加密源串:" + contents);
        }
        return ret;
    }
    /**
     * MD5 加密
     * @param source
     * @param charset
     * @return
     */
    public static String md5Encrypt(String source,String charset){
        String ret  = "";
        try {
            ret =  MD5Encrypt.encrypt(source,charset);
        } catch (Exception e) {
            logger.error("failed to generate external_sign_no",e);
        }
        return ret;
    }

    /**
     * 解析XML
     * @param xml
     * @param path xpath
     * @return
     */
    public static String parseXmlResultFromAlipay(String xml,String path){
        org.dom4j.Document document;
        try {
            document = DocumentHelper.parseText(xml);
            List items = document.selectNodes("/"+path);
            for (Object node : items) {
                if (node instanceof Element) {
                    Element element = (Element) node;
                    return element.getStringValue();
                }
            }
        } catch (DocumentException e) {
            logger.error("解析支付宝返回的XML出错",e);
        }
        return "F";
    }
    
    /**
     * 生成退款请求url
     * @param paygateway
     * @param service
     * @param sign_type
     * @param batch_no
     * @param input_charset
     * @param partner
     * @param notify_url
     * @param refund_date
     * @param batch_num
     * @param detail_data
     * @param priKey
     * @return
     */
	public static String dsaRefundSign(String paygateway, String service, String sign_type, String batch_no,
			String input_charset, String partner,String notify_url,String refund_date,String batch_num,String detail_data,String priKey) {
		Properties properties = new Properties();
		properties.put("service", service);
		properties.put("partner", partner);
		properties.put("_input_charset", input_charset);
		properties.put("sign_type", sign_type);
		properties.put("batch_no", batch_no);
		
		properties.put("notify_url", notify_url);
		properties.put("refund_date", refund_date);//格式为：yyyy-MM-dd hh:mm:ss。
		properties.put("batch_num", batch_num);//退款总笔数
		properties.put("detail_data", detail_data);//单笔数据集
		String sign = null;
		try {
			DSASignature dsaSignature = new DSASignature();
			sign = dsaSignature.sign(getSignatureContent(properties), priKey, input_charset);
			logger.info("Snail refund order sign: " + sign);
		} catch (Exception e) {
			logger.error("", e);
		}
		String url = null;
		try {
			url = paygateway + getURLContent(properties, sign, input_charset, sign_type);
		} catch (Exception e) {
			logger.error("", e);
		}
		return url;
	}
	/**
	 * RSA退款签名
	 * @param paygateway
	 * @param service
	 * @param sign_type
	 * @param batch_no
	 * @param input_charset
	 * @param partner
	 * @param notify_url
	 * @param refund_date
	 * @param batch_num
	 * @param detail_data
	 * @param priKey
	 * @return
	 */
	public static String rsaRefundSign(String paygateway, String service, String sign_type, String batch_no,
			String input_charset, String partner,String notify_url,String refund_date,String batch_num,String detail_data,String priKey) {
		Properties properties = new Properties();
		properties.put("service", service);
		properties.put("partner", partner);
		properties.put("_input_charset", input_charset);
		properties.put("batch_no", batch_no);
		
		properties.put("notify_url", notify_url);
		properties.put("refund_date", refund_date);//格式为：yyyy-MM-dd hh:mm:ss。
		properties.put("batch_num", batch_num);//退款总笔数
		properties.put("detail_data", detail_data);//单笔数据集
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(getRefundSignatureContent(properties), priKey,"");
			logger.info("Snail check order sign: " + sign);
		} catch (Exception e) {
			logger.error("", e);
		}
		String url = null;
		try {
			url = paygateway + getURLContent(properties, sign, input_charset, sign_type);
		} catch (Exception e) {
			logger.error("", e);
		}
		return url;
	}
	
	/**
	 * 参数排序
	 * 过滤空值、sign与sign_type参数
	 * 生成待签名字符串
	 * @param properties
	 *            支付参数
	 * @return 排序后字符串
	 */
	@SuppressWarnings("unchecked")
	public static String getRefundSignatureContent(Properties properties) {
		StringBuffer content = new StringBuffer();
		List keys = new ArrayList(properties.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = properties.getProperty(key);
			if (key.equalsIgnoreCase("id") || value == null || value.trim().length() == 0 || key.equalsIgnoreCase("sign")
	                || key.equalsIgnoreCase("sign_type")) {
				continue;
			}
			content.append((i == 0 ? "" : "&") + key + "=" + value);
		}
		return content.toString();
	}
	
	
	
	public static void main(String[] args) throws Exception{
//		String sourceStr = "_input_charset=utf-8&batch_no=2015101314102920020000000008&batch_num=2&detail_data=2015101321001004790035665512^0.01^协商退款1#2015101321001004790035336445^0.01^协退款2&notify_url=http://222.92.116.36:90/payment/backend/api/common/2088021843865588/2002&partner=2088611105733025&refund_date=2015-10-15 18:01:45&service=refund_fastpay_by_platform_nopwd";
//		System.out.println("sourceStr:"+sourceStr);
//		String priKey = AlipayHelper.readText("C:\\Users\\fuzl\\Desktop\\o2o\\o2o_snail_rsa_private_key.txt");//REFUND_PRIVATE_KEY_FILE
//		
//		String sign = EncryptFactory.getInstance(Rsa.NAME).sign(sourceStr, priKey,"");
//		System.out.println("sign:"+sign);
//		
//		String pubKey = AlipayHelper.readText("C:\\Users\\fuzl\\Desktop\\o2o\\o2o_snail_rsa_public_key.pem");//REFUND_PUBLIC_KEY_FILE;
//		boolean ret = EncryptFactory.getInstance(Rsa.NAME).verify(sign, sourceStr, pubKey, "UTF-8");
//		System.out.println("ret:"+ret);
	}
}
