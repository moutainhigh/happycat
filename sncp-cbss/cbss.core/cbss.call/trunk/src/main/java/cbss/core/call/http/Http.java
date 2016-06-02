package cbss.core.call.http;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cbss.core.call.http.alert.AlertService;
import cbss.core.trace.logformat.LogFormat;
import cbss.core.util.DateUtils;
import cbss.core.util.IpUtils;

import com.alibaba.fastjson.JSONObject;

@Component
@ConfigurationProperties(value = "cbss.api.alert.http.conf")
public class Http {
	private final Logger logger = LoggerFactory.getLogger(Http.class);

	@Autowired
	private LogFormat logFormat;

	@Autowired
	private AlertService alertService;

	private String alertUrl;
	private String alertServiceType;

	private int alertMaxcycle;

	private int alertTimeout;

	private String alertContent;

	public Map<String, Object> post(String requsetUrl, Map<String, Object> headers, Map<String, Object> paramMaps, int timeout, String encode)
			throws Exception {
		return post(requsetUrl, headers, paramMaps, timeout, encode, true);
	}

	public Map<String, Object> post(String requsetUrl, Map<String, Object> headers, Map<String, Object> paramMaps, int timeout, String encode, boolean needToMap)
			throws Exception {
		long start = System.currentTimeMillis();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		long startTime = System.currentTimeMillis();
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

			HttpPost httpPost = new HttpPost(requsetUrl);

			// 2015.1.4 add 缩短httpclient调用时间，4.0.1有此问题
			httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

			// Execute HTTP request
			if (headers != null && headers.size() > 0) {
				Set<String> keys = headers.keySet();
				for (String key : keys) {
					String value = ObjectUtils.toString(headers.get(key));
					httpPost.setHeader(key, value);
				}
			}
			// httpPost.setHeader("Content-type", "application/xml");

			List<NameValuePair> list = new ArrayList<NameValuePair>();
			if (paramMaps != null && paramMaps.size() > 0) {
				Set<String> keys = paramMaps.keySet();
				for (String key : keys) {
					String value = ObjectUtils.toString(paramMaps.get(key));
					list.add(new BasicNameValuePair(key, value));
				}
			}

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, encode);
			httpPost.setEntity(entity);
			HttpResponse response = httpclient.execute(httpPost);
			resultMap.put("HTTPSTATEINFO", response.getStatusLine().getStatusCode());

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				InputStreamReader reader = new InputStreamReader(resEntity.getContent(), encode);
				char[] buff = new char[1024];
				int length = 0;
				while ((length = reader.read(buff)) != -1) {
					result += new String(buff, 0, length);
				}
				resultMap.put("HTTPRSPINFO", result);
			}

			httpclient.getConnectionManager().shutdown();
			if (needToMap) {
				resultMap.putAll(JSONObject.parseObject(result));
			}
			resultMap.put("originalStr", result);
			resultMap.put("httpclient", httpclient);
			return resultMap;
		} catch (Exception e) {
			try {
				Map<String, Object> extend = new HashMap<String, Object>();
				extend.put("timedelay", (System.currentTimeMillis() - start));
				extend.put("machineip", (IpUtils.getLoaclAddr()));
				alertService.submitAlertMsg(alertUrl, alertServiceType,
						String.format(alertContent, DateUtils.format(new Date(), DateUtils.TIMESTAMP_MS), requsetUrl, String.valueOf(paramMaps), e.getMessage(), timeout), alertMaxcycle, alertTimeout,
						extend);
			} catch (Exception e1) {
				logger.error(logFormat.format("HttpPost", "HttpPost", "HttpPost", headers + "|" + String.valueOf(paramMaps), String.valueOf(startTime),
						String.valueOf(System.currentTimeMillis() - startTime), requsetUrl, IpUtils.getLoaclAddr(), e1.getMessage(), true));
			}
			throw e;
		} finally {
			httpclient.getConnectionManager().shutdown();
			logger.error(logFormat.format("HttpPost", "HttpPost", "HttpPost", headers + "|" + String.valueOf(paramMaps), String.valueOf(startTime),
					String.valueOf(System.currentTimeMillis() - startTime), requsetUrl, IpUtils.getLoaclAddr(), result, true));
		}
	}

	public Map<String, Object> post(String requsetUrl, Map<String, Object> headers, String postData, int timeout, String encode)
			throws Exception {
		long start = System.currentTimeMillis();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		long startTime = System.currentTimeMillis();
		StringBuffer result = new StringBuffer();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

			HttpPost httpPost = new HttpPost(requsetUrl);

			// 2015.1.4 add 缩短httpclient调用时间，4.0.1有此问题
			httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

			if (headers != null && headers.size() > 0) {
				Set<String> keys = headers.keySet();
				for (String key : keys) {
					String value = ObjectUtils.toString(headers.get(key));
					httpPost.setHeader(key, value);
				}
			}

			StringEntity entity = new StringEntity(postData, encode);
			httpPost.setEntity(entity);

			HttpResponse response = httpclient.execute(httpPost);
			resultMap.put("HTTPSTATEINFO", response.getStatusLine().getStatusCode());

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				InputStreamReader reader = new InputStreamReader(resEntity.getContent(), encode);
				char[] buff = new char[1024];
				int length = 0;
				while ((length = reader.read(buff)) != -1) {
					result.append(new String(buff, 0, length));
				}
				resultMap.put("HTTPRSPINFO", result.toString());
			}
			return resultMap;
		} catch (Exception e) {
			try {
				Map<String, Object> extend = new HashMap<String, Object>();
				extend.put("timedelay", (System.currentTimeMillis() - start));
				extend.put("machineip", (IpUtils.getLoaclAddr()));
				alertService.submitAlertMsg(alertUrl,alertServiceType, String.format(alertContent, DateUtils.format(new Date(), DateUtils.TIMESTAMP_MS), requsetUrl, postData, e.getMessage(), timeout),
						alertMaxcycle, alertTimeout, extend);
			} catch (Exception e1) {
				logger.error(logFormat.format("HttpPost", "HttpPost", "HttpPost", headers + "|" + postData, String.valueOf(startTime), String.valueOf(System.currentTimeMillis() - startTime),
						requsetUrl, IpUtils.getLoaclAddr(), e1.getMessage(), true));
			}
			throw e;
		} finally {
			httpclient.getConnectionManager().shutdown();
			logger.error(logFormat.format("HttpPost", "HttpPost", "HttpPost", timeout + "|" + headers + "|" + postData, String.valueOf(startTime),
					String.valueOf(System.currentTimeMillis() - startTime), requsetUrl, IpUtils.getLoaclAddr(), result.toString(), true));
		}
	}

	public String getCookieValue(DefaultHttpClient httpclient, String name) {
		if (httpclient == null) {
			return null;
		}
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		if (cookies.isEmpty()) {
			return null;
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getName().equalsIgnoreCase(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public AlertService getAlertService() {
		return alertService;
	}

	public void setAlertService(AlertService alertService) {
		this.alertService = alertService;
	}

	public String getAlertServiceType() {
		return alertServiceType;
	}

	public void setAlertServiceType(String alertServiceType) {
		this.alertServiceType = alertServiceType;
	}

	public int getAlertMaxcycle() {
		return alertMaxcycle;
	}

	public void setAlertMaxcycle(int alertMaxcycle) {
		this.alertMaxcycle = alertMaxcycle;
	}

	public int getAlertTimeout() {
		return alertTimeout;
	}

	public void setAlertTimeout(int alertTimeout) {
		this.alertTimeout = alertTimeout;
	}

	public String getAlertContent() {
		return alertContent;
	}

	public void setAlertContent(String alertContent) {
		this.alertContent = alertContent;
	}

	public String getAlertUrl() {
		return alertUrl;
	}

	public void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}

}
