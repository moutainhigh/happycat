package com.woniu.sncp.pay.core;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.json.JsonUtils;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

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

	public static Response callGet(String requsetUrl, Map<String, ?> headers, int timeout) throws Exception {
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

	public static String get(String requsetUrl, Map<String, ?> headers, int timeout) throws Exception {

		Response resp = callGet(requsetUrl, headers, timeout);
		String result = resp.body().string();

		logger.info("result:" + result);
		return result;
	}
	// 和JS fetch接口使用一致
	// https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch

	public static Response fetch(String url, Map<String, Object> initRequest) throws IOException {
		String method = null;
		Object body = null;
		Map<String, Object> headers = new HashMap<String, Object>();
		Request.Builder builder = new Request.Builder();
		String contentType = null;
		int timeout = 5000;

		if (initRequest != null) {
			for (Map.Entry<String, Object> entry : initRequest.entrySet()) {
				if (StringUtils.equalsIgnoreCase(entry.getKey(), "method")) {
					method = ObjectUtils.toString(entry.getValue());
				} else if (StringUtils.equalsIgnoreCase(entry.getKey(), "body")) {
					body = entry.getValue();
				} else if (StringUtils.equalsIgnoreCase(entry.getKey(), "headers")) {
					if (entry.getValue() instanceof Map) {
						headers.putAll((Map) entry.getValue());
					}
				} else if (StringUtils.equalsIgnoreCase(entry.getKey(), "timeout")) {
					try {
						timeout = Integer.parseInt(entry.getValue().toString());
					} catch (Exception e) {

					}
				} else {
					logger.info("igree param {}:{}", entry.getKey(), entry.getValue());
				}
			}

		}

		if (!headers.isEmpty()) {
			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				String value = ObjectUtils.toString(entry.getValue());
				if (StringUtils.equalsIgnoreCase(entry.getKey(), "content-type")) {
					contentType = value;
				}
				builder.addHeader(entry.getKey(), value);
			}

		}
		if (StringUtils.equalsIgnoreCase("post", method)) {

			String content_type = StringUtils.isBlank(contentType) ? "application/x-www-form-urlencoded;charset=utf-8" : contentType;
			builder.addHeader("content-type", content_type);
			if (body instanceof CharSequence) {
				builder.post(RequestBody.create(MediaType.get(contentType), body.toString()));
			} else if (body instanceof Map) {
				Map<?, ?> _body = (Map) body;
				if (StringUtils.containsIgnoreCase(content_type, "json")) {
					builder.post(RequestBody.create(MediaType.get(contentType), JsonUtils.toJson(body)));
				} else {
					FormBody.Builder bodyBuilder = new FormBody.Builder();
					for (Map.Entry<?, ?> entry : _body.entrySet()) {
						bodyBuilder.add(ObjectUtils.toString(entry.getKey()), ObjectUtils.toString(entry.getValue()));
					}
					builder.post(bodyBuilder.build());

				}
			} else {
				builder.post(Util.EMPTY_REQUEST);
			}

			builder.url(url);

		} else {

			if (body instanceof CharSequence) {

				if (url.contains("?")) {
					builder.url(url + "&" + body.toString());
				} else {
					builder.url(url + "?" + body.toString());
				}

			} else if (body instanceof Map) {
				String queryString = parseQueryString((Map) body, true);
				if (url.contains("?")) {
					builder.url(url + "&" + queryString);
				} else {
					builder.url(url + "?" + queryString);
				}
			}else {
				builder.url(url);
			}

		}
		OkHttpClient client = creater(timeout);

		Response resp = client.newCall(builder.build()).execute();
		return resp;

	}

	private static String parseQueryString(Map<String, ?> params, boolean encode) {
		List<String> elements = new ArrayList<String>();
		for (Map.Entry<String, ?> entry : params.entrySet()) {
			String value;
			try {
				value = encode ? URLEncoder.encode(ObjectUtils.toString(entry.getValue()), "utf-8") : ObjectUtils.toString(entry.getValue());
				elements.add(entry.getKey() + "=" + value);
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			}

		}
		return StringUtils.join(elements, "&");
	}
}
