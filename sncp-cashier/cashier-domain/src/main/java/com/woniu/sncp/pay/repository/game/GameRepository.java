package com.woniu.sncp.pay.repository.game;



import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>descrption: 账号操作</p>
 * 
 * @author fuzl
 * @date   2017年3月22日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Transactional
public interface GameRepository extends JpaRepository<Game, Long> {

}
