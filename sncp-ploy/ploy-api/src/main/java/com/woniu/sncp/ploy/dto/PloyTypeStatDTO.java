package com.woniu.sncp.ploy.dto;

import java.io.Serializable;
import java.util.List;

import com.woniu.sncp.ploy.dto.LargessPropDTO;
import com.woniu.sncp.ploy.dto.PloyPropDTO;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;



/**
 * 活动类型统计结果
 * @author chenyx
 *
 */
public class PloyTypeStatDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private State state;
	
	public enum State {
		NEXT, OVER
	}
	
	//活动类型
	private PresentsPloyDTO presentsPloy;
	
	//活动使用的道具信息
	private List<PloyPropDTO> ployProps;
	
	//赠送明细信息
	private List<LargessPropDTO> largessProps;

	public PloyTypeStatDTO(PresentsPloyDTO presentsPloy) {
		this.presentsPloy = presentsPloy;
	}
	
	public PresentsPloyDTO getPresentsPloy() {
		return presentsPloy;
	}

	public void setPresentsPloy(PresentsPloyDTO presentsPloy) {
		this.presentsPloy = presentsPloy;
	}

	public List<PloyPropDTO> getPloyProps() {
		return ployProps;
	}

	public void setPloyProps(List<PloyPropDTO> ployProps) {
		this.ployProps = ployProps;
	}

	public List<LargessPropDTO> getLargessProps() {
		return largessProps;
	}

	public void setLargessProps(List<LargessPropDTO> largessProps) {
		this.largessProps = largessProps;
	}

	public void addLargessProp(LargessPropDTO largessProp) {
		this.largessProps.add(largessProp);
	}
	
	public void addPolyProp(PloyPropDTO ployProp) {
		this.ployProps.add(ployProp);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
