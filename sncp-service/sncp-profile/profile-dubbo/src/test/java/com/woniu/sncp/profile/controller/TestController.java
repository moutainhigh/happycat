package com.woniu.sncp.profile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.sncp.profile.dto.PaginationTo;
import com.woniu.sncp.profile.service.DownConfigService;

@Controller
public class TestController {
	
	@Autowired DownConfigService service;
	
	@RequestMapping("/test")
    public @ResponseBody PaginationTo test() {
        return service.query("1", "1", 3, 1);
    }
}
