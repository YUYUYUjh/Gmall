package com.yy.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Yu
 * @create 2021-10-16 11:21
 */
@Controller
public class UserController {


    /**
     * 去登录页面
     * @return
     */
    @GetMapping("/login.html")
    public String toLogin(String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "login";
    }
}
