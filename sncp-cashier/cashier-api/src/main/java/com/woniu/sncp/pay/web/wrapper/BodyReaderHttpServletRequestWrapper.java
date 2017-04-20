package com.woniu.sncp.pay.web.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

/**
 * <p>
 * descrption: 重写servlet的getInputStream支持request多次读取
 * </p>
 * 
 * @author fuzl
 * @date 2016年5月23日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class BodyReaderHttpServletRequestWrapper extends
		HttpServletRequestWrapper {

	private byte[] requestBody = null;

	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		// 缓存请求body
		try {
			requestBody = StreamUtils.copyToByteArray(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 重写 getInputStream()
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (requestBody == null) {
			requestBody = new byte[0];
		}
		final ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return bais.read();
			}

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setReadListener(ReadListener listener) {
				// TODO Auto-generated method stub
				
			}

		};
	}

	/**
	 * 重写 getReader()
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

}
