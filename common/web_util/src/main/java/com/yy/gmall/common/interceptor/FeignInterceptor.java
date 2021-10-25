package com.yy.gmall.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

//@Component  //放入spring容器只要是依赖这个模块就会实例化这个对象,使用不到的也会实例化,需要使用自己new
public class FeignInterceptor implements RequestInterceptor {

    //在远程调用之前执行
    //参数 : 马上要发出的请求对象
    @Override
    public void apply(RequestTemplate requestTemplate){

        //在springIOC容器 任意位置 获取HttpServletRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes!=null){
            HttpServletRequest request = attributes.getRequest();
            if (request!=null){
                //  添加header
                if (!StringUtils.isEmpty(request.getHeader("userTemId"))){

                    requestTemplate.header("userTempId", request.getHeader("userTempId"));
                }
                if (!StringUtils.isEmpty(request.getHeader("userId"))){

                    requestTemplate.header("userId", request.getHeader("userId"));
                }
            }
        }
    }
}
