package com.woniu.sncp.pay.common.utils.encrypt;

/**
 * 签名接口
 * @author luzz
 *
 */
public abstract class Signature implements Encryption{
	
	/**
	 * 数据加密
	 * @param data
	 * @param key
	 * @param encode
	 * @return
	 */
	public String encrypt(String data, String key,String encode) throws Exception{return null;};
	
	/**
	 * 数据解密
	 * @param data
	 * @param key
	 * @param encode
	 * @return
	 */
	public String decrypt(String data, String key,String encode) throws Exception{return null;};
	
	
}
