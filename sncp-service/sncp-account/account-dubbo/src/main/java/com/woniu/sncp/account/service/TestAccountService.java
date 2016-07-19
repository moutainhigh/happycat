/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.service;

import com.alibaba.fastjson.JSON;
import com.woniu.sncp.account.dto.OcpAccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: Test
 * @Description:
 * @Author zhujing
 * @Date 2016/7/8
 * @Version V1.0
 */
@Controller
public class TestAccountService {

    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/abc", method = RequestMethod.GET)
    public String testQueryBalance(){

        Map<String, Object> m = new HashMap<String,Object>();
        m.put("abc", "def");
        Object json = JSON.toJSON(m);

        Object obj = accountService.queryBalance(1L, 7, "36", "-1", null, "-1", "2016-07-08 15:18:00", json, "10101007");

        System.out.println( obj.toString() );

        return obj.toString();

    }

    @RequestMapping(value = "/def", method = RequestMethod.GET)
    public String testEasyImprest(){

        Map<String, Object> m = new HashMap<String,Object>();
        m.put("method", "easyImprest");
        Object json = JSON.toJSON(m);

        OcpAccountDTO ocpAccountDTO = accountService.easyImprest(1L, "36", "-1", "o", "1", "1", "", json, "10101007");

        System.out.println( ocpAccountDTO.toString() );

        return ocpAccountDTO.toString();
    }


}
