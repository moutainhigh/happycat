package com.woniu.sncp.vip.service;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.woniu.sncp.exception.SystemException;
import com.woniu.sncp.vip.dto.PassportVipDTO;
import com.woniu.sncp.vip.entity.PassportVip;
import com.woniu.sncp.vip.entity.PassportVipPK;
import com.woniu.sncp.vip.entity.PassportVipPresents;
import com.woniu.sncp.vip.repository.PassportVipPresentsRepository;
import com.woniu.sncp.vip.repository.PassportVipRepository;

@Service
public class PassportVipServiceImpl implements PassportVipService {

	@Autowired
	private PassportVipRepository passportVipRepository;

	@Autowired
	private PassportVipPresentsRepository passportVipPresentsRepository;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public PassportVipDTO findPassportVipByAidAndGameId(Long aid, Long gameId) throws SystemException {
		String vipLevel = null;
		PassportVipPK passportVipPK = new PassportVipPK();
		passportVipPK.setAid(aid);
		passportVipPK.setGameId(gameId);
		PassportVip passportVip = passportVipRepository.findById(passportVipPK);
		if (!passportVip.getState().equals("1")) {
			return null;
		}
		Page<PassportVipPresents> passportVipPresents = passportVipPresentsRepository
				.findBySendLevel(passportVip.getSendVipLevel(), new PageRequest(0, 1));
		Date sendTime = DateUtils.addMonths(passportVip.getSendTime(),
				passportVipPresents.getContent().get(0).getMonth());
		if (Integer.valueOf(passportVip.getSendVipLevel()).intValue() > Integer.valueOf(passportVip.getVipLevel())
				.intValue() && sendTime.after(new Date())) {
			vipLevel = passportVip.getSendVipLevel();
		} else {
			vipLevel = passportVip.getVipLevel();
		}
		PassportVipDTO passportVipDTO = new PassportVipDTO();
		passportVipDTO.setAid(aid);
		passportVipDTO.setVipLevel(vipLevel);
		return passportVipDTO;
	}

}
