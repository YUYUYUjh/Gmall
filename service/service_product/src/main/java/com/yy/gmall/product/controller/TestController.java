package com.yy.gmall.product.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yu
 * @create 2021-10-10 9:12
 */
@RestController
@RequestMapping("/admin/product")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/test/testLock")
    public Result testLock(){
        return Result.ok();
    }

}
