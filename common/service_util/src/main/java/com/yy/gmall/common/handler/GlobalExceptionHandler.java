package com.yy.gmall.common.handler;

import com.yy.gmall.common.execption.GmallException;
import com.yy.gmall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileNotFoundException;

/**
 * 全局异常处理类
 *    在项目中只要抛出异常  try 除了自己捕获以外 所有异常被 当前实例类所捕获
 *    有小的抛小的 没有小的抛大的
 *    GmallException
 *
 * @author qy
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody //此注解 直接把返回结果抛到页面上    //去掉此注解 ：可以跳转指定页面
    public Result error(Exception e){
        e.printStackTrace();//打印在控制台
        return Result.fail(); //将异常返回到页面上  抛异常到页面
        //企业做法： 跳转错误页面   服务器太忙  美工前端 美丽页面 提示
    }

    /**
     * 自定义异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(GmallException.class)
    @ResponseBody
    public Result error(GmallException e){
        return Result.fail(e.getMessage());
    }
    /**
     * 自定义异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseBody
    public Result error111(GmallException e){
        //处理方案
        return Result.fail(e.getMessage());
    }
}
