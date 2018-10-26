package com.woniu.sncp.pay.core;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	protected static OkHttpClient creater(int timeout) {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
		builder.readTimeout(timeout, TimeUnit.MILLISECONDS);
		builder.writeTimeout(timeout, TimeUnit.MILLISECONDS);

		return builder.build();
	}
 public static Response callPost(String requsetUrl, Map<String, Object> headers, String body, int timeout) throws Exception {
		OkHttpClient client = creater(timeout);
		Request.Builder builder = new Request.Builder();
		builder.url(requsetUrl);
		String contentType = null;
		if (headers != null) {
			for (Map.Entry<String, Object> entity : headers.entrySet()) {
				String name = entity.getKey();
				String value = ObjectUtils.toString(entity.getValue());
				builder.addHeader(name, value);
				if (StringUtils.equalsIgnoreCase(name, "contentType")) {

					contentType = value;
				}
			}

		}
		if (StringUtils.isBlank(contentType)) {
			contentType = "application/json;charset=utf-8";
		}

		logger.info("requsetUrl:" + requsetUrl);
		logger.info("headers:" + headers);
		logger.info("body:" + body);
		builder.post(RequestBody.create(MediaType.get(contentType), body));
		Response resp = client.newCall(builder.build()).execute();
	return resp;
	}
	/**
	 * 
	 * 
	 * @param requsetUrl
	 * @param headers
	 * @param body
	 * @param timeout
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String post(String requsetUrl, Map<String, Object> headers, String body, int timeout) throws Exception {
	 
		Response resp = callPost(requsetUrl, headers, body, timeout);
		String result = resp.body().string();
 
		logger.info("result:" + result);
		return result;
	}
	
	public static Response callGet(String requsetUrl, Map<String, ?> headers , int timeout) throws Exception {
		OkHttpClient client = creater(timeout);
		Request.Builder builder = new Request.Builder();
		builder.url(requsetUrl);
		String contentType = null;
		if (headers != null) {
			for (Map.Entry<String, ?> entity : headers.entrySet()) {
				String name = entity.getKey();
				String value = ObjectUtils.toString(entity.getValue());
				builder.addHeader(name, value);
				if (StringUtils.equalsIgnoreCase(name, "contentType")) {

					contentType = value;
				}
			}

		}
		if (StringUtils.isBlank(contentType)) {
			contentType = "application/json;charset=utf-8";
		}

		logger.info("requsetUrl:" + requsetUrl);
		logger.info("headers:" + headers);
 		builder.get();
		Response resp = client.newCall(builder.build()).execute();
		return resp;
  
	 
	}
	
	public static String get(String requsetUrl, Map<String, ?> headers , int timeout) throws Exception {
 
		Response resp =callGet(requsetUrl, headers, timeout);
		String result = resp.body().string();
 
		logger.info("result:" + result);
		return result;
	}
	
}
