package com.yy.gmall.order;

import com.yy.gmall.common.interceptor.FeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yu
 * @create 2021-10-19 19:08
 */
@SpringBootApplication
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.yy.gmall"})
public class ServiceOrderApplication {
    public static void main(String[] args)
        {
            SpringApplication.run(ServiceOrderApplication.class,args);
        }

    //手动实例化远程调用拦截器
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
