package com.woniu.sncp.cbss.core.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异步线程池,通过spring注入初始化参数
 * 
 * @author Yang Hao
 * 
 */
@Component
@ConfigurationProperties(value="cbss.api.properties")
public class PropertiesUtl {

	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
