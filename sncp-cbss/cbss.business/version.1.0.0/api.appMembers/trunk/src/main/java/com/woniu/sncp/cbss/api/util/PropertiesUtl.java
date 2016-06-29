package com.woniu.sncp.cbss.api.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author mizy
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
