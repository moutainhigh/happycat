package com.woniu.sncp.pay.core.service.payment.platform.boa;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
public class Header {
	  private String secretKey = "YOURSECRETKEY";
	  private String storeId = "10";
	  private String contentMD5;
	  private String httpVerb;

	  public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public Header(String url, String content) throws NoSuchAlgorithmException, MalformedURLException, UnsupportedEncodingException {
	    this.setContentMd5(content);
	    this.setHttpVerb(new URL(url));
	  }

	  private void setHttpVerb(URL url) {
	    this.httpVerb = url.getPath() + (url.getQuery() != null ? url.getQuery() : "");
	  }

	  private void setContentMd5(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    if (content == "") {
	      this.contentMD5 = "";
	    }else {
	      MessageDigest md = MessageDigest.getInstance("MD5");
	      byte messageDigest[] = md.digest(content.getBytes("UTF-8"));
	      this.contentMD5 = Base64.encodeBase64String(new BigInteger(1,messageDigest).toString(16).getBytes());
	    }
	  }

	  private String generateAuthorization() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
	    final String data = this.httpVerb+this.contentMD5;
	    Mac mac = Mac.getInstance("HmacSHA256");
	    mac.init(new SecretKeySpec(this.secretKey.getBytes("UTF8"), "HmacSHA256"));
	    return Hex.encodeHexString(mac.doFinal(data.getBytes("UTF-8")));
	  }

	  public HashMap<String,String> generateHeader() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
	    HashMap<String, String> headers = new HashMap<>();

	    headers.put("Accept", "application/vnd.boacompra.com.v1+json; charset=UTF-8");
	    headers.put("Content-Type", "application/json");
	    headers.put("Content-MD5", this.contentMD5);
	    headers.put("Authorization", this.storeId+':'+this.generateAuthorization());
	    headers.put("Accept-Language", "en-US");

	    return headers;
	  }
	}