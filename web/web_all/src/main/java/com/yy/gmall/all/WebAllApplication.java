package com.yy.gmall.all;

import com.yy.gmall.common.interceptor.FeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.yy.gmall"})
public class WebAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class, args);
    }

    //手动实例化远程调用拦截器
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}