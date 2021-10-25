package com.yy.gmall.all.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-12 14:54
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 主页
     * @param model
     * @return
     */
    @GetMapping("/")
    public String index(Model model){
        List<Map> categoryList = productFeignClient.getCategoryList();
        model.addAttribute("list",categoryList);
        return "index/index";
    }


    /**
     * 生成静态页面
     * @return
     */
    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml(){
        //1.静态化页面的过程
        //2.数据
        List<Map> categoryList = productFeignClient.getCategoryList();
        Context context = new Context();
        context.setVariable("list",categoryList);
        //3.模板
        String templateName = "index/index";
        //4.输出流
        Writer out = null;
        try {
            //写
            //out = new FileWriter("D:\\index.html");
            //out = new OutputStreamWriter(new FileOutputStream("D:\\index.html"),"UTF-8");
            out = new PrintWriter("D:\\index.html","UTF-8");
            //读
            templateEngine.process(templateName,context,out);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Result.ok();
    }
}
