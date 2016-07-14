package com.woniu.sncp.cbss.core.authorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.bigdullrock.spring.boot.nifty.NiftyHandler;
import com.woniu.sncp.cbss.core.authorize.nifty.NiftyParam;
import com.woniu.sncp.cbss.core.model.request.RequestDatas;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

@Component
@ConfigurationProperties(prefix = "cbss.api.access.request", locations = { "classpath:accessurl.properties" })
public class AccessUrlConfigurationProperties {
	/*
	 * spring.redis.cluster.nodes[0] = 127.0.0.1:7379
	 * spring.redis.cluster.nodes[1] = 127.0.0.1:7380 ...
	 */
	List<String> urls;
	List<String> paramTypes;
	List<RequestDatas<RequestParam>> paramObjects;

	@Autowired
	private ApplicationContext applicationContext;

	public List<RequestDatas<RequestParam>> getParamObjects() {
		return paramObjects;
	}

	public void setParamObjects(List<RequestDatas<RequestParam>> paramObjects) {
		this.paramObjects = paramObjects;
	}

	public List<String> getParamTypes() {
		return paramTypes;
	}

	@PostConstruct
	public void buildParamObject() {
		try {
			if (paramTypes != null && !paramTypes.isEmpty()) {
				paramObjects = new ArrayList<RequestDatas<RequestParam>>();
				for (String paramType : paramTypes) {
					Object object = Class.forName(paramType).newInstance();
					if (object instanceof RequestDatas<?>) {
						paramObjects.add((RequestDatas<RequestParam>) object);
					}
				}
			}

			for (Map.Entry<String, Object> niftyParam : applicationContext.getBeansWithAnnotation(NiftyParam.class).entrySet()) {
				String name = niftyParam.getValue().toString();
				urls.add(StringUtils.substring(name, 0, StringUtils.indexOf(name, '@')));
			}

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setParamTypes(List<String> paramTypes) {
		this.paramTypes = paramTypes;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
}
