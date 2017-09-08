package com.woniu.sncp.pay.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.sncp.json.JsonUtils;

public abstract class ContentUtils {
	private static Set<String> filter = new HashSet<String>(Arrays.asList("pwd", "password","cardPwd"));

	public static String safeLogJson(Map<String, Object> params) {
		Map<String, Object> inParams = new HashMap<String, Object>();
		if (params != null) {
			inParams.putAll(params);
			inParams.remove(PaymentConstant.PAYMENT_ORDER);
			inParams.remove(PaymentConstant.PAYMENT_PLATFORM);
			inParams.remove(RefundmentConstant.HTTP_REQUEST);

			inParams.remove(RefundmentConstant.PAYMENT_PLATFORM);
			inParams.remove(RefundmentConstant.REFUNDMENT_BATCH);
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.convertValue(params, JsonNode.class);
		mask(node, filter);

		return JsonUtils.toJson(node);
	}

	public static String mask(String key, String value, Set<String> fileds) {
		if (key == null || value == null || fileds == null || CollectionUtils.isEmpty(fileds))
			return value;
		if (fileds.contains(key.toUpperCase())) {
			if (value.length() > 2)
				return new String(new char[] { value.charAt(0), '*', '*', '*', value.charAt(value.length() - 1) });
			return "*****";
		}

		return value;
	}

	public static void mask(JsonNode json, Set<String> fileds) {
		if (json == null)
			return;
		if (json.isArray()) {
			ArrayNode node = (ArrayNode) json;
			Iterator<JsonNode> iterator = node.iterator();
			while (iterator.hasNext()) {
				mask(iterator.next(), fileds);
			}

		} else if (json.isObject()) {
			ObjectNode node = (ObjectNode) json;
			Iterator<Entry<String, JsonNode>> iterator = node.getFields();
			while (iterator.hasNext()) {
				Entry<String, JsonNode> next = iterator.next();
				String key = next.getKey();
				JsonNode value = next.getValue();
				if (value == null)
					continue;
				if (value.isTextual()) {
					String string = value.asText();
					String mask = mask(key, string, fileds);
					if (string != mask) {
						node.put(key, mask);
					}

				} else if (value.isContainerNode()) {
					mask(next.getValue(), fileds);
				}

			}

		}

	}
}
