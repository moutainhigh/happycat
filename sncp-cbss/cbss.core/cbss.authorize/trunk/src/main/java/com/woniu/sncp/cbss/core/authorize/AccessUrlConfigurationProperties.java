package com.woniu.sncp.cbss.core.authorize;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.woniu.sncp.cbss.core.authorize.nifty.NiftyParam;
import com.woniu.sncp.cbss.core.authorize.rest.WoniuRequestData;
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

	Map<String, RequestDatas<RequestParam>> url2ParamObject;

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
			if (url2ParamObject == null) {
				url2ParamObject = new HashMap<String, RequestDatas<RequestParam>>();
			}

			for (Map.Entry<String, Object> niftyParam : applicationContext.getBeansWithAnnotation(NiftyParam.class).entrySet()) {
				String name = niftyParam.getValue().toString();
				url2ParamObject.put(StringUtils.substring(name, 0, StringUtils.indexOf(name, '@')), null);
			}

			for (Map.Entry<String, Object> entry : applicationContext.getBeansWithAnnotation(WoniuRequestData.class).entrySet()) {
				Annotation[] ans = entry.getValue().getClass().getAnnotations();
				String name = entry.getValue().toString();
				for (Annotation annotation : ans) {
					if (annotation instanceof WoniuRequestData) {
						Object object = Class.forName(StringUtils.substring(name, 0, StringUtils.indexOf(name, '@'))).newInstance();
						if (object instanceof RequestDatas<?>) {
							url2ParamObject.put(((WoniuRequestData) annotation).uri(), (RequestDatas<RequestParam>) object);
						}
					}
				}
			}

			if (paramTypes != null && !paramTypes.isEmpty()) {
				paramObjects = new ArrayList<RequestDatas<RequestParam>>();
				for (String paramType : paramTypes) {
					Object object = Class.forName(paramType).newInstance();
					if (object instanceof RequestDatas<?>) {
						paramObjects.add((RequestDatas<RequestParam>) object);
					}
				}
			}

			if (urls == null) {
				urls = new ArrayList<String>();
			}
			if (paramTypes == null) {
				paramTypes = new ArrayList<String>();
			}
			if (paramObjects == null) {
				paramObjects = new ArrayList<RequestDatas<RequestParam>>();
			}

			if (!url2ParamObject.isEmpty()) {
				for (Entry<String, RequestDatas<RequestParam>> entry : url2ParamObject.entrySet()) {
					urls.add(entry.getKey());
					RequestDatas<RequestParam> entryValue = entry.getValue();
					if (null == entryValue) {
						paramTypes.add(RequestDatas.class.getClass().getName());
						paramObjects.add(new RequestDatas<RequestParam>());
					} else {
						paramTypes.add(entryValue.getClass().getName());
						paramObjects.add(entryValue);
					}
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
}
