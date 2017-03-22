package com.woniu.sncp.pay.common.utils.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	public static String post(String url, Map<String, String> nameValuePair) throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();

		String result = "";
		try {
			HttpPost hp = new HttpPost(url);

			//httpclient.getParams().setParameter("http.protocol.content-charset", "utf-8");
			// 连接超时 30s
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时 30s
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);

			List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
			for (Map.Entry<String, String> entry : nameValuePair.entrySet()) {
				formparams.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}

			hp = new HttpPost(url);
			//添加http头信息  模拟浏览器
			//hp.addHeader("Content-Type", "text/html;charset=UTF-8");  
			//hp.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");  
			//hp.addHeader("Referer",url);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,"UTF-8");
			hp.setEntity(entity);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			result = httpclient.execute(hp, responseHandler);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		return result;
	}
}
