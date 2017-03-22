package com.woniu.sncp.pay.common.utils.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.net.NetServiceException;

/**
 * 支付验证接口工具类
 * 
 * 1.支持https
 * 2.支持requst body
 * 
 * @author luzz
 *
 */
public class PayCheckUtils {
	private static final Logger logger = LoggerFactory.getLogger(PayCheckUtils.class);
	
	public static String postRequst(String url,Map<String,Object> params,int timeout,String encode,String logName) throws NetServiceException {
		HttpClient httpclient = new DefaultHttpClient();
		if(url.indexOf("https") == 0){
			httpclient = WebClientDevWrapper.wrapClient(httpclient);
		}
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,encode);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		
		logger.info("url:" + url);
		logger.info("params:" + params);
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		Iterator<String> iter = params.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = String.valueOf(params.get(key));
			formparams.add(new BasicNameValuePair(key, value));
		}
		String response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, encode);
			httpPost.setEntity(entity);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			if(url.indexOf("https") > 0){
				supportHttps(httpclient);
			}
			response = httpclient.execute(httpPost,responseHandler);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (KeyManagementException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		}  finally {
			logger.info(logName+"接口 url:{},返回:{}",url,response);
			abortConnection(httpPost, httpclient);
		}
		
		return response;
	}
	
	public static String postBodyRequst(String url,String body,int timeout,String encode,String logName) throws NetServiceException {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,encode);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		
		logger.info("url:" + url);
		logger.info("body:" + body);
		
		String response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			StringEntity entity = new StringEntity(body,encode);
			httpPost.setEntity(entity);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			if(url.indexOf("https") > 0){
				supportHttps(httpclient);
			}
			response = httpclient.execute(httpPost,responseHandler);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (KeyManagementException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException(logName+"出错，请与客服联系", e);
		}  finally {
			logger.info(logName+"接口 url:{} 返回:{}",url,response);
			abortConnection(httpPost, httpclient);
		}
		
		return response;
	}
	
	/**
	 * 释放HttpClient连接
	 * 
	 * @param hrb
	 *            请求对象
	 * @param httpclient
	 * 			  client对象
	 */
	private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
	/**
	 * 支持Https
	 * 
	 * @param httpClient
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void supportHttps(HttpClient httpClient) throws NoSuchAlgorithmException, KeyManagementException{
		// First create a trust manager that won't care.
		X509TrustManager trustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// Don't do anything.
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// Don't do anything.
			}

			public X509Certificate[] getAcceptedIssuers() {
				// Don't do anything.
				return null;
			}

		};
		// Now put the trust manager into an SSLContext.
		SSLContext sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init(null, new TrustManager[] { trustManager }, null);

		// Use the above SSLContext to create your socket factory
		// (I found trying to extend the factory a bit difficult due to a
		// call to createSocket with no arguments, a method which doesn't
		// exist anywhere I can find, but hey-ho).
		SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sf, 443));
	}
}
