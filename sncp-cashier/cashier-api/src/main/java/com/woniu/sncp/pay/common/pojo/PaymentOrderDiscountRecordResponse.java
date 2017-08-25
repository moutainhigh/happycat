package com.woniu.sncp.pay.common.pojo;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.woniu.sncp.pojo.payment.PaymentOrderDiscountRecord;

public class PaymentOrderDiscountRecordResponse extends PaymentOrderDiscountRecord {
 
	
	public static class CustomJsonDateSerializer extends JsonSerializer<Date> {

		@Override
		public void serialize(Date aDate, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider)
				throws IOException, JsonProcessingException {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = dateFormat.format(aDate);
			aJsonGenerator.writeString(dateString);
		}

	}
	public static class CustomJsonFloatSerializer extends JsonSerializer<Float> {

		@Override
		public void serialize(Float aDate, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider)
				throws IOException, JsonProcessingException {
		 
	        
	        aJsonGenerator.writeString(new BigDecimal(aDate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		}

	}
	@JsonSerialize(using = CustomJsonFloatSerializer.class)
	@Override
	public Float getMoney() {
		// TODO Auto-generated method stub
		return super.getMoney();
	}
	@JsonSerialize(using = CustomJsonDateSerializer.class)
	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return super.getCreateDate();
	}
}
