package com.woniu.sncp.ploy.service;

import com.woniu.sncp.ploy.dto.PloyRequestDTO;
import com.woniu.sncp.ploy.dto.PloyResponseDTO;

/**
 * 活动服务接口
 * @author chenyx
 *
 */
public interface PloyService {

	/**
	 * 查询活动
	 * @param ployRequest
	 * @return
	 * @throws Exception
	 */
	public PloyResponseDTO queryPloy(PloyRequestDTO ployRequest) throws Exception;
}
