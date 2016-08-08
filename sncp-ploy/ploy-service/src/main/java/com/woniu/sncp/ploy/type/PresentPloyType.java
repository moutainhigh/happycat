package com.woniu.sncp.ploy.type;

import org.springframework.messaging.Message;

import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;

public interface PresentPloyType {

	public PloyTypeStatDTO doPloy(Message<PresentsPloyDTO> presentsPloy) throws Exception;
	
}
