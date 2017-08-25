package com.woniu.sncp.pay.common.pojo;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;



/**
 * 针对jackson的Date类型反序列化
 * @author chenyx
 *
 */
public class CustomJsonDateDeSerializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String value = p.readValueAs(String.class);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return dateFormat.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
