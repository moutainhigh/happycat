package com.woniu.sncp.pay.core.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.woniu.sncp.pay.repository.pay.Game;
import com.woniu.sncp.pojo.game.GameArea;
import com.woniu.sncp.pojo.game.GameServer;


/**
 * 游戏相关管理
 *
 * @author yanghao 2010-3-16
 *
 */

/**
 * @author Administrator
 */
public interface GameManagerService {

    /**
     * 查询所有游戏
     *
     * @return
     */
    public List<Game> queryGames();
    
    /**
     * 查询所有启动状态的游戏
     * @return
     */
    public List<Game> queryAllOpenGmes();


    /**
     * 查询所有分区
     *
     * @return
     */
    public List<GameArea> queryGameAreasList();


    public List<GameArea> queryGameAreasListWithOutJiuyin();

    /**
     * 查询指定时间内开服的游戏分区
     *
     * @param gameId
     * @param startTime
     * @param endTime
     * @return
     * @throws DataAccessException
     */
    List<GameArea> queryGameRealms(Long gameId, Date startTime, Date endTime) throws DataAccessException;

    /**
     * 游戏对应的游戏分区。
     *
     * @param gameId
     * @return
     * @throws DataAccessException
     */
    List<GameArea> queryGameRealms(Long gameId) throws DataAccessException;

	/**
	 * 查询游戏分区信息及所在组ID
	 * 
	 * @param gameId
	 * @param issuerId
	 * @param isShowGroupName
	 *            true 显示组名称,false 为不显示组名称
	 * @param isToLowerCaseMapOfKey
	 *            true 将key名称转小写,false 为不转
	 * @param excludeGameId
	 *            过滤游戏ID
	 * @return
	 * @throws DataAccessException
	 */
	List<Map<String, Object>> queryGameGroupArea(Long gameId, Long issuerId, boolean isShowGroupName,
			boolean isToLowerCaseMapOfKey, Date startTime, Date endTime, Long excludeGameId,Map<String,Object> params) throws DataAccessException;

	List<Map<String, Object>> queryGameGroupArea(Long gameId, Long issuerId, boolean isShowGroupName,
			boolean isToLowerCaseMapOfKey, Date startTime, Date endTime, Long excludeGameId) throws DataAccessException;

    /**
     * 游戏对应的游戏分区,如果没有分区就不要显示了。
     *
     * @param gameId
     * @return
     * @throws DataAccessException
     */
    List queryGameRealmsByIsp(Long gameId) throws DataAccessException;



    /**
     * 判断游戏和分区是否对应。
     *
     * @param gameId
     * @param areaId
     * @return
     * @throws DataAccessException
     */
    boolean checkGameByArea(long gameId, long areaId) throws DataAccessException;


    /**
     * 根据游戏分区查询分站数据库
     *
     * @param gameAreaId
     * @return 分区数据库id
     */
    public String queryGameDB(Long gameAreaId);


    /**
     * 根据ID查询游戏
     *
     * @param gameId
     * @return
     * @throws DataAccessException
     */
    Game queryGameById(long gameId) throws DataAccessException;

    /**
     * 根据ID查询游戏
     *
     * @param areaId
     * @return
     * @throws DataAccessException
     */
    GameArea queryGameAreaById(long areaId) throws DataAccessException;

    /**
     * 根据分区ID查询启用的服务器
     *
     * @param gameAreaId
     * @return
     * @throws DataAccessException
     */
    List<GameServer> queryGameServerByGameAreaId (long gameAreaId) throws DataAccessException;

    /**
     * 加载网络类型
     * @return
     */
    List<GameArea> queryNetTypeList() throws DataAccessException;
    
    /**
     * 根据服务器id查询启用的服务器
     * @param gameServerId
     * @return
     * @throws DataAccessException
     */
    GameServer queryGameServerById(long gameServerId) throws DataAccessException;
}
