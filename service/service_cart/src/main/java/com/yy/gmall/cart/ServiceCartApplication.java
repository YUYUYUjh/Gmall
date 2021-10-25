package com.yy.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Yu
 * @create 2021-10-17 17:24
 */
@SpringBootApplication
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.yy.gmall"})
@EnableAsync //开启异步线程
public class ServiceCartApplication {
    public static void main(String[] args)
        {
            SpringApplication.run(ServiceCartApplication.class,args);
        }

}
