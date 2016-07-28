package com.woniu.sncp.ploy.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;
import com.woniu.sncp.ploy.service.PresentPloyService;

/**
 * 活动参与者创建工厂
 * @author chenyx
 *
 */
@Component
public class PloyParticipatorFactory {
	
	@Autowired
	private PresentPloyService presentPloyService;

	/**
	 * 创建活动参与者
	 * @param ployRequest
	 * @return
	 * @throws Exception
	 */
	@ServiceActivator
	public PloyParticipator create(PloyRequestDTO ployRequest) throws Exception {
		PloyParticipator participator = new PloyParticipator();
		participator.setPloyRequest(ployRequest);
		List<PresentsPloyDTO> presentsPloys = presentPloyService.findByGameId(ployRequest.getGame(), ployRequest.getEventTime());
		participator.setPresentsPloys(presentsPloys);
		return participator;
	}
}
