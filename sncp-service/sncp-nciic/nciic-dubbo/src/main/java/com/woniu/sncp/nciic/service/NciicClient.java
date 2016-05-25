package com.woniu.sncp.nciic.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.woniu.sncp.nciic.wsdl.NciicCheck;
import com.woniu.sncp.nciic.wsdl.NciicCheckResponse;

public class NciicClient extends WebServiceGatewaySupport {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@PostConstruct
	private void setMarshaller() {
		getWebServiceTemplate().setMarshaller(marshaller);
		getWebServiceTemplate().setUnmarshaller(marshaller);
	}

	public NciicCheckResponse nciicCheckResponse(String conditions, String license) {

		NciicCheck nciicCheck = new NciicCheck();
		nciicCheck.setInConditions(conditions);
		nciicCheck.setInLicense(license);

		NciicCheckResponse response = (NciicCheckResponse) getWebServiceTemplate().marshalSendAndReceive("http://ngs.nciic.net.cn/nciic_ws/services/NciicServices", nciicCheck,
				new SoapActionCallback("http://ngs.nciic.net.cn/nciic_ws/services/NciicCheck"));
		return response;
	}
}
