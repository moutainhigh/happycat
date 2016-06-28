package com.woniu.sncp.profile.service;


import java.util.stream.Collectors;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.woniu.sncp.profile.dto.DownConfigTo;
import com.woniu.sncp.profile.dto.PaginationTo;
import com.woniu.sncp.profile.jpa.DownConfigRepository;
import com.woniu.sncp.profile.po.DownConfigPo;

public class DownConfigServiceImpl implements DownConfigService{

	protected static final Logger log = LoggerFactory.getLogger(DownConfigServiceImpl.class);
	
	@Autowired DownConfigRepository repository;
	
	@Override
	public PaginationTo query(String type, String osType,int pageSize,int pageNumber) {
		PageRequest pageable =  new PageRequest(pageNumber - 1, pageSize);
		Page<DownConfigPo> result = repository.findByTypeAndOsTypeOrderBySortAscCreateAsc(type, osType, pageable);
		
		PaginationTo to = new PaginationTo();
		
		to.setDownConfigList(result.getContent()
									.stream()
									.map(o -> new DozerBeanMapper().map(o, DownConfigTo.class))
									.collect(Collectors.toList()));
		
		to.setPageNumber(pageNumber);
		to.setPageSize(pageSize);
		to.setTotalSize(result.getTotalElements());
		
		return to;
	}

}
