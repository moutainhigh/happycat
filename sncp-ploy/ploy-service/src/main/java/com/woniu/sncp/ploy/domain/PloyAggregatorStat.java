package com.woniu.sncp.ploy.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.ReleaseStrategy;
import org.springframework.messaging.Message;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.woniu.sncp.ploy.dto.PloyResponseDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;

/**
 * 赠送聚合统计
 * 
 * @author chenyx
 *
 */
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
		return new PloyResponseDTO();
	}

	@ReleaseStrategy
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
	}

}
