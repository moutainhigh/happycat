package com.woniu.sncp.pay.common.utils.encrypt;

/**
 * 加密接口
 * @author luzz
 *
 */
public interface Encryption {
	
	String ENCODE_UTF8="utf8";
	String ENCODE_GBK="gbk";
	
	/**
	 * 数据加密
	 * @param data
	 * @param key
	 * @param encode
	 * @return
	 */
	String encrypt(String data, String key,String encode) throws Exception;
	
	/**
	 * 数据解密
	 * @param data
	 * @param key
	 * @param encode
	 * @return
	 */
	String decrypt(String data, String key,String encode) throws Exception;
	
	
	/**
	 * 数字签名
	 * @param data 原串
	 * @param key 密钥
	 * @return
	 */
	String sign(String data,String key,String encode) throws Exception;
	
	
	/**
	 * 数字签名验证
	 * @param sign 签名
	 * @param data 原串
	 * @param key 密钥
	 * @return
	 */
	Boolean verify(String sign,String data,String key,String encode);
	
}
