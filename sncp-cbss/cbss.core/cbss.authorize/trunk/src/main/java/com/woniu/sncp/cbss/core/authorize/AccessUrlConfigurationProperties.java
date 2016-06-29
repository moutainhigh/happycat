package com.woniu.sncp.cbss.core.authorize;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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

	List<String> services;
	List<String> methods;
	List<String> requestparams;

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
			
			if (services != null && !services.isEmpty()) {
				if (urls == null) {
					urls = new ArrayList<String>();
				}
				for (int i = 0; i < services.size(); i++) {
					urls.add(services.get(i) + "." + methods.get(i) + "." + requestparams.get(i));
				}
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

	public List<String> getServices() {
		return services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public List<String> getRequestparams() {
		return requestparams;
	}

	public void setRequestparams(List<String> requestparams) {
		this.requestparams = requestparams;
	}
}
