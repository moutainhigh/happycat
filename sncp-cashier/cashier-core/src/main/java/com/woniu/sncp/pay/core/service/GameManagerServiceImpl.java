package com.woniu.sncp.pay.core.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.repository.game.Game;
import com.woniu.sncp.pay.repository.game.GameRepository;
import com.woniu.sncp.pojo.game.GameArea;
import com.woniu.sncp.pojo.game.GameServer;

@Service("gameManagerService")
public class GameManagerServiceImpl implements GameManagerService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
    GameRepository gameRepository;
    
    @Override
    public Game queryGameById(long gameId) throws DataAccessException {
        return gameRepository.getOne(gameId);
    }

	@Override
	public List<Game> queryGames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Game> queryAllOpenGmes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameArea> queryGameAreasList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameArea> queryGameAreasListWithOutJiuyin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameArea> queryGameRealms(Long gameId, Date startTime, Date endTime) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameArea> queryGameRealms(Long gameId) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> queryGameGroupArea(Long gameId, Long issuerId, boolean isShowGroupName,
			boolean isToLowerCaseMapOfKey, Date startTime, Date endTime, Long excludeGameId, Map<String, Object> params)
					throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> queryGameGroupArea(Long gameId, Long issuerId, boolean isShowGroupName,
			boolean isToLowerCaseMapOfKey, Date startTime, Date endTime, Long excludeGameId)
					throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List queryGameRealmsByIsp(Long gameId) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkGameByArea(long gameId, long areaId) throws DataAccessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String queryGameDB(Long gameAreaId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameArea queryGameAreaById(long areaId) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameServer> queryGameServerByGameAreaId(long gameAreaId) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameArea> queryNetTypeList() throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameServer queryGameServerById(long gameServerId) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}
