package com.woniu.sncp.nciic.service;

import java.util.List;

import com.woniu.sncp.exception.BusinessException;
import com.woniu.sncp.nciic.dto.NciicMessageOut;
import com.woniu.sncp.nciic.dto.NciicMessageIn;

/**
 * 实名验证
 * 
 */
public interface NciicMessageService {

	/**
	 * 告警消息异步发送
	 * 
	 * @param monitorMessage
	 *            {@link com.woniu.sncp.NciicMessageIn.dto.AlarmMessageTo}
	 * @return
	 * @throws DocumentException
	 * @throws BusinessException
	 */
	NciicMessageOut checkRealNameIdentityNo(NciicMessageIn nciicMessageIn)
			throws NciicException;

	List<NciicMessageOut> checkRealNameIdentityNo(List<NciicMessageIn> nciicMessageIns)
			throws NciicException;
}
