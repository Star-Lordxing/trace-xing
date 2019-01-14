package com.trace.server.trace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /**
     * 查询服务名和接口名
     */
    @GetMapping(value = "/index")
    public String index() {
        return "index";
    }

}
