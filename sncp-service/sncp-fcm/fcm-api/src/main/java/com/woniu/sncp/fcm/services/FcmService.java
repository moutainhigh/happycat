package com.woniu.sncp.fcm.services;

import com.woniu.sncp.fcm.dto.PassportFcmTotalTimeTo;

public interface FcmService {
	boolean isFcm(Long accountId,Long gameId);
	Long queryFcmTime(Long accountId,Long gameId);
	PassportFcmTotalTimeTo queryUserFcmTotalTime(Long accountId,Long gameId);
}
