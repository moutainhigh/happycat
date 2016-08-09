package com.woniu.sncp.ploy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.ploy.dto.PresentsPloyDTO;
import com.woniu.sncp.ploy.entity.PresentsPloy;
import com.woniu.sncp.ploy.repository.PresentsPloyDao;
import com.woniu.sncp.ploy.repository.PresentsPloyRepository;

@Service
public class PresentPloyServiceImpl implements PresentPloyService {
	
	protected static final Logger log = LoggerFactory.getLogger(PresentPloyServiceImpl.class);
	
	@Autowired private PresentsPloyDao presentsPloyDao;

	@Autowired
	private PresentsPloyRepository presentsPloyRepository;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public List<PresentsPloyDTO> findByGameId(String gameId, Date eventTime) throws Exception {
		List<PresentsPloy> presentsPloyEntities = presentsPloyRepository.findByLimitGameAndState(gameId, eventTime);
		DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
		return new ArrayList<PresentsPloyDTO>(
				Collections2.transform(presentsPloyEntities, new Function<PresentsPloy, PresentsPloyDTO>() {
					@Override
					public PresentsPloyDTO apply(PresentsPloy input) {
						return dozerBeanMapper.map(input, PresentsPloyDTO.class);
					}

				}));
	}


}
