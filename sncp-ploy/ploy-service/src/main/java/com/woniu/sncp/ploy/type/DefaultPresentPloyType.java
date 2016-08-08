package com.woniu.sncp.ploy.type;

import org.springframework.messaging.Message;

import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;

/**
 * 默认活动
 * @author chenyx
 *
 */
public class DefaultPresentPloyType implements PresentPloyType {

	@Override
	public PloyTypeStatDTO doPloy(Message<PresentsPloyDTO> presentsPloy) throws Exception {
		return new PloyTypeStatDTO(presentsPloy.getPayload());
	}

}
