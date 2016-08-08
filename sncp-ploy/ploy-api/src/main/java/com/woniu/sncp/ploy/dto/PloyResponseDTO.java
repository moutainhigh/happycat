package com.woniu.sncp.ploy.dto;

import java.io.Serializable;
import java.util.List;

public class PloyResponseDTO implements Serializable{

	private static final long serialVersionUID = 5371411137413620459L;
	
	private List<PloyTypeStatDTO> ployTypeStats;

	public List<PloyTypeStatDTO> getPloyTypeStats() {
		return ployTypeStats;
	}

	public void setPloyTypeStats(List<PloyTypeStatDTO> ployTypeStats) {
		this.ployTypeStats = ployTypeStats;
	}
	
}
