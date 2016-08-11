package com.woniu.sncp.imprest.service;

import java.util.Date;
import java.util.List;

import org.codehaus.groovy.util.StringUtil;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.imprest.dto.ImprestCardTypeDTO;
import com.woniu.sncp.imprest.dto.ImprestLogDTO;
import com.woniu.sncp.imprest.dto.ImprestOrderDTO;
import com.woniu.sncp.imprest.entity.ImprestCardType;
import com.woniu.sncp.imprest.entity.ImprestLog;
import com.woniu.sncp.imprest.entity.ImprestOrder;
import com.woniu.sncp.imprest.entity.LargessPoints;
import com.woniu.sncp.imprest.repository.ImprestCardTypeRepository;
import com.woniu.sncp.imprest.repository.ImprestLogDao;
import com.woniu.sncp.imprest.repository.ImprestLogRepository;
import com.woniu.sncp.imprest.repository.ImprestOrderRepository;
import com.woniu.sncp.imprest.repository.LargessPointsRepository;

@Service
public class ImprestServiceImpl implements ImprestService {
	
	@Autowired
	protected ImprestLogDao imprestLogDao;
	
	@Autowired
	private ImprestLogRepository imprestLogRepository;
	
	@Autowired
	private ImprestOrderRepository imprestOrderRepository;
	
	@Autowired
	private ImprestCardTypeRepository imprestCardTypeRepository;
	
	@Autowired
	private LargessPointsRepository largessPointsRepository;

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

	@Override
	public List<ImprestLogDTO> queryImprestLogs(Long aid, Long gameId, Long areaId, List<Long> platformIds,
			Date startDate, Date endDate, List<Long> speCards) throws SystemException {
		
		List<ImprestLogDTO> impLogList = imprestLogDao.queryImprestLogs(aid, gameId, areaId, platformIds, startDate, endDate, speCards);
		
		return impLogList;
	}

	@Override
	public Long findSumLargessPoints(Long aid, Date start, Date end, String currency, String sourceType)
			throws SystemException {
		if(StringUtils.hasText(sourceType)) {
			return largessPointsRepository.sumAmountByAidAndCreateDateAndCurrencyAndSourceType(aid, start, end, currency, sourceType);
		} else {
			return largessPointsRepository.sumAmountByAidAndCreateDateAndCurrency(aid, start, end, currency);
		}
	}

}
