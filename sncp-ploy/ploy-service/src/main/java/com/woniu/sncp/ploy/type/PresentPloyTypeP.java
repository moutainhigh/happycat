package com.woniu.sncp.ploy.type;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import com.woniu.sncp.ploy.domain.PloyTypeStatDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;

public class PresentPloyTypeP implements PresentPloyType {

	@ServiceActivator
	public PloyTypeStatDTO doPloy(Message<PresentsPloyDTO> presentsPloy) throws Exception {
		// TODO Auto-generated method stub
		return new PloyTypeStatDTO(presentsPloy.getPayload());
	}

}
