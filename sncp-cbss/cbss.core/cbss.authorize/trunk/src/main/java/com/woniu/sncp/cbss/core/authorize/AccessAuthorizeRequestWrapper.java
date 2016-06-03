package com.woniu.sncp.cbss.core.authorize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

import com.woniu.sncp.cbss.core.authorize.exception.AccessAuthorizeException;

public class AccessAuthorizeRequestWrapper extends HttpServletRequestWrapper {

	private String body;

	private ServletInputStream servletInputStream;
	private ByteArrayInputStream byteArrayInputStream;
	private ServletInputStream requestInputStream;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public AccessAuthorizeRequestWrapper(HttpServletRequest request) {
		super(request);
		try {
			InputStream inputStream = getRequest().getInputStream();
			body = IOUtils.toString(inputStream, "utf-8");

			body = body.trim();
		} catch (IOException ex) {
			throw new AccessAuthorizeException("Error reading the request body", ex);
		} finally {
		}
	}

	/**
	 * Override of the getInputStream() method which returns an InputStream that
	 * reads from the stored XML payload string instead of from the request's
	 * actual InputStream.
	 */
	@Override
	public ServletInputStream getInputStream()
			throws IOException {

		byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

		requestInputStream = getRequest().getInputStream();

		servletInputStream = new ServletInputStream() {

			@Override
			public boolean isFinished() {
				return requestInputStream.isFinished();
			}

			@Override
			public boolean isReady() {
				return requestInputStream.isReady();
			}

			@Override
			public void setReadListener(ReadListener arg0) {
				requestInputStream.setReadListener(arg0);
			}

			@Override
			public int read()
					throws IOException {
				return byteArrayInputStream.read();
			}
		};

		return servletInputStream;
	}

	/**
	 * 关闭servletInputStream、byteArrayInputStream、requestInputStream
	 */
	public void clear() {
		if (servletInputStream != null) {
			try {
				servletInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (byteArrayInputStream != null) {
			try {
				byteArrayInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (requestInputStream != null) {
			try {
				requestInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
