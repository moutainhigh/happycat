package com.woniu.sncp.imprest.service;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;
import com.woniu.sncp.imprest.dto.ImprestOrderDTO;
import com.woniu.sncp.imprest.entity.ImprestCardType;
import com.woniu.sncp.imprest.entity.ImprestLog;
import com.woniu.sncp.imprest.entity.ImprestOrder;
import com.woniu.sncp.imprest.repository.ImprestCardTypeRepository;
import com.woniu.sncp.imprest.repository.ImprestLogRepository;
import com.woniu.sncp.imprest.repository.ImprestOrderRepository;

@Service
public class ImprestServiceImpl implements ImprestService {
	
	@Autowired
	private ImprestLogRepository imprestLogRepository;
	
	@Autowired
	private ImprestOrderRepository imprestOrderRepository;
	
	@Autowired
	private ImprestCardTypeRepository imprestCardTypeRepository;

	@Override
	public ImprestLogDTO findImprestLogById(Long implogId) throws SystemException {
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		ImprestLog imprestLog = imprestLogRepository.findOne(implogId);
		return beanMapper.map(imprestLog, ImprestLogDTO.class);
	}

	@Override
	public ImprestOrderDTO findImprestOrderByOrderNoAndGameAreaIdAndAid(String orderNo, Long gameAreaId, Long aid)
			throws SystemException {
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		ImprestOrder imprestOrder = imprestOrderRepository.findImprestOrderByOrderNoAndGameAreaIdAndAid(orderNo, gameAreaId, aid);
		return beanMapper.map(imprestOrder, ImprestOrderDTO.class);
	}

	@Override
	public ImprestCardTypeDTO findImprestCardById(Long cardId) throws SystemException {
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		ImprestCardType imprestCardType = imprestCardTypeRepository.findOne(cardId);
		return beanMapper.map(imprestCardType, ImprestCardTypeDTO.class);
	}

}
