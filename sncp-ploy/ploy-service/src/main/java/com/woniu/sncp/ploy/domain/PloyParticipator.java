package com.woniu.sncp.ploy.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;

/**
 * 活动参与者
 * @author chenyx
 *
 */
public class PloyParticipator implements Serializable {

	private static final long serialVersionUID = 5631235372970872979L;
	
	//前端请求参数
	private PloyRequestDTO ployRequest;
	
	//可参与的活动列表
	private List<PresentsPloyDTO> presentsPloys;

	public List<PresentsPloyDTO> getPresentsPloys() {
		return presentsPloys;
	}

	public void setPresentsPloys(List<PresentsPloyDTO> presentsPloys) {
		this.presentsPloys = presentsPloys;
	}


	public PloyRequestDTO getPloyRequest() {
		return ployRequest;
	}

	public void setPloyRequest(PloyRequestDTO ployRequest) {
		this.ployRequest = ployRequest;
	}
	
	public Boolean isSatisfiedBy(List<PresentsPloyDTO> presentsPloys)  {
		Collection<PresentsPloyDTO> unsatisfiedPresentsPloys = findMissingPresentsPloys(presentsPloys);
		return unsatisfiedPresentsPloys.isEmpty();
	}
	
	 private Collection<PresentsPloyDTO> findMissingPresentsPloys(final List<PresentsPloyDTO> presentsPloys) {
	        return Collections2.filter(presentsPloys, new Predicate<PresentsPloyDTO>() {
	            public boolean apply(PresentsPloyDTO input) {
	                for (PresentsPloyDTO presentsPloy : presentsPloys) {
	                    if (input.equals(presentsPloy)) {
	                        return false;
	                    }
	                }
	                return true;
	            }
	        });
	    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ployRequest == null) ? 0 : ployRequest.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PloyParticipator other = (PloyParticipator) obj;
		if (ployRequest == null) {
			if (other.ployRequest != null)
				return false;
		} else if (!ployRequest.equals(other.ployRequest))
			return false;
		return true;
	}
	 
}
