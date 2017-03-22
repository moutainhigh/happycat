package com.woniu.sncp.pay.common.utils.http;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.holders.IntHolder;

import org.apache.commons.lang.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.json.JsonUtils;

public class HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	/**
	 * 参数以params={"key1":"value1","key2":"value2"}的形式提交
	 * @param requsetUrl
	 * @param headers
	 * @param paramMaps
	 * @param timeout
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String post(String requsetUrl,Map<String,Object> headers,Map<String,Object> paramMaps,int timeout,String encode) throws Exception {
		return post(requsetUrl, headers, paramMaps, timeout, encode,true);
	}
	
	/**
	 * 参数以普通方式传递 key1=value1,key2=value2
	 * @param requsetUrl
	 * @param headers
	 * @param paramMaps
	 * @param timeout
	 * @param encode
	 * @param flag flag=true时，参数以params={"key1":"value1","key2":"value2"}的形式提交
	 * @return
	 * @throws Exception
	 */
	public static String post(String requsetUrl,Map<String,Object> headers,Map<String,Object> paramMaps,int timeout,String encode,boolean flag) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		logger.info("requsetUrl:" + requsetUrl);
		logger.info("headers:"+headers);
		logger.info("params:" + paramMaps);
		
		HttpPost httpPost = new HttpPost(requsetUrl);
		String result = "";
		// Execute HTTP request
		if (headers != null && headers.size() > 0) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				String value = ObjectUtils.toString(headers.get(key));
				httpPost.setHeader(key, value);
			}
		}
		//httpPost.setHeader("Content-type", "application/xml");

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if(flag){
			list.add(new BasicNameValuePair("params", JsonUtils.toJson(paramMaps)));
		}else{
			if (paramMaps != null && paramMaps.size() > 0) {
				Set<String> keys = paramMaps.keySet();
				for (String key : keys) {
					String value = ObjectUtils.toString(paramMaps.get(key));
					list.add(new BasicNameValuePair(key, value));
				}
			}
		}
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, encode);
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity resEntity = response.getEntity();
		InputStreamReader reader = new InputStreamReader(resEntity.getContent(),encode);
		char[] buff = new char[1024];
		int length = 0;
		while ((length = reader.read(buff)) != -1) {
			result += new String(buff, 0, length);
		}
		httpclient.getConnectionManager().shutdown();
		logger.info("result:" + result);
		return result;
	}
	
	/**
	 * 参数以普通方式传递 key1=value1,key2=value2
	 * @param requsetUrl
	 * @param headers
	 * @param paramMaps
	 * @param timeout
	 * @param encode
	 * @param flag flag=true时，参数以params={"key1":"value1","key2":"value2"}的形式提交
	 * @return
	 * @throws Exception
	 */
	public static String post(String requsetUrl,Map<String,Object> headers,String params,int timeout,String encode) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		logger.info("requsetUrl:" + requsetUrl);
		logger.info("headers:"+headers);
		logger.info("params:" + params);
		
		HttpPost httpPost = new HttpPost(requsetUrl);
		String result = "";
		// Execute HTTP request
		if (headers != null && headers.size() > 0) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				String value = ObjectUtils.toString(headers.get(key));
				httpPost.setHeader(key, value);
			}
		}
		
		StringEntity entity = new StringEntity(params, encode);
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity resEntity = response.getEntity();
		InputStreamReader reader = new InputStreamReader(resEntity.getContent(),encode);
		char[] buff = new char[1024];
		int length = 0;
		while ((length = reader.read(buff)) != -1) {
			result += new String(buff, 0, length);
		}
		httpclient.getConnectionManager().shutdown();
		logger.info("result:" + result);
		return result;
	}

	public static String post(String requsetUrl, Map<String, Object> headers,
			String params, int timeout, String encode, IntHolder d) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		logger.info("requsetUrl:" + requsetUrl);
		logger.info("headers:"+headers);
		logger.info("params:" + params);
		
		HttpPost httpPost = new HttpPost(requsetUrl);
		String result = "";
		// Execute HTTP request
		if (headers != null && headers.size() > 0) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				String value = ObjectUtils.toString(headers.get(key));
				httpPost.setHeader(key, value);
			}
		}
		
		StringEntity entity = new StringEntity(params, encode);
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);
		d.value = response.getStatusLine().getStatusCode();//response响应码
		HttpEntity resEntity = response.getEntity();
		InputStreamReader reader = new InputStreamReader(resEntity.getContent(),encode);
		char[] buff = new char[1024];
		int length = 0;
		while ((length = reader.read(buff)) != -1) {
			result += new String(buff, 0, length);
		}
		httpclient.getConnectionManager().shutdown();
		logger.info("result:" + result);
		return result;
	}
}
