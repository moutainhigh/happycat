package com.woniu.sncp.ploy.integration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.woniu.sncp.ploy.dto.PloyResponseDTO;
import com.woniu.sncp.ploy.dto.PloyTypeStatDTO;

/**
 * 赠送聚合统计
 * 
 * @author chenyx
 *
 */
@Component
public class PloyAggregatorStat {

	@CorrelationStrategy
	public Object correlatingPloyParticipatorFor(Message<PloyTypeStatDTO> message) {
		return message.getHeaders().get("ployParticipator");
	}

	@Aggregator
	public PloyResponseDTO preparePloyResponse(List<Message<PloyTypeStatDTO>> ployTypeStats) {
		List<PloyTypeStatDTO> list = new ArrayList<PloyTypeStatDTO>();
		for (Message<PloyTypeStatDTO> ployTypeStat : ployTypeStats) {
			System.out.println(ployTypeStat.toString());
			list.add(ployTypeStat.getPayload());
		}
		PloyResponseDTO ployResponseDTO = new PloyResponseDTO();
		ployResponseDTO.setPloyTypeStats(list);
		return ployResponseDTO;
	}

	/*@ReleaseStrategy
	public boolean canRelease(List<Message<PloyTypeStatDTO>> ployTypeStats) {
		PloyParticipator ployParticipator = (PloyParticipator) ployTypeStats.get(0).getHeaders()
				.get("ployParticipator");
		return ployParticipator.isSatisfiedBy(presentPloyFromMessages(ployTypeStats));
	}

	private ArrayList<PresentsPloyDTO> presentPloyFromMessages(List<Message<PloyTypeStatDTO>> ployTypeStats) {
		return new ArrayList<PresentsPloyDTO>(
				Collections2.transform(ployTypeStats, new Function<Message<PloyTypeStatDTO>, PresentsPloyDTO>() {
					public PresentsPloyDTO apply(Message<PloyTypeStatDTO> ployTypeStat) {
						return ployTypeStat.getPayload().getPresentsPloy();
					}
				}));
	}*/

}
