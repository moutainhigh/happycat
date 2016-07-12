/**
 * 
 */
package com.woniu.sncp.profile.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.woniu.sncp.profile.ProfileApplication;
import com.woniu.sncp.profile.dto.CardDetailDTO;
import com.woniu.sncp.profile.dto.GameAreaDTO;
import com.woniu.sncp.profile.dto.GameGroupDTO;

/**
 * @author fuzl
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ProfileApplication.class)
public class GameCardActivityManageServiceImplTest {

	@Autowired
	GameManageService gameManageService;
	
	
	
//	@Test
	public void testGameQuery(){
		Long gameId = 10L;
		Long issuerId = 7L;
		Boolean isShowGroupName = false;
		Boolean isToLowerCase = true;
		String excludeGameId = "";
		String state = "1";
		String type = "1";
		//获取游戏网络类型和分区
		List<GameGroupDTO> dtoList = gameManageService.findByGameIdAndStateAndType(gameId,state,type);
		for(GameGroupDTO dto:dtoList){
			System.out.println(dto.getGroupName());
		}
		//根据游戏服务器id获取分区id
		Long serverId = 7100039L;
		GameAreaDTO dto= gameManageService.findByServerId(serverId);
		System.out.println(dto.getName() + ":"+ dto.getId());
		
		//根据游戏id查询面值信息
	}
	
	
	@Autowired
	CardTypeManageService cardManageService;
	
	//@Test
	public void testCardQuery(){
		Long gameId = 17L;
		Long paymentId = 449L;
//		List<CardValueDTO> dtoList = cardManageService.findValueByGameIdAndPlatformId(gameId, platformId);
//		for(CardValueDTO dto:dtoList){
//			System.out.println(dto.getDispName());
//		}
		List<CardDetailDTO> dtoList = cardManageService.findDetailByGameIdAndPlatformId(gameId, paymentId);
		for(CardDetailDTO dto:dtoList){
			System.out.println(dto.toString());
		}
	}
	
	
	@Autowired
	ActivityManageService activityManageService;
	
	@Test
	public void testActivityQuery(){
		String state = "3";
		Long gameId = 10L;
		activityManageService.findAllPloysByState(gameId,state);
	}
}
