package com.yy.gmall.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yu
 * @create 2021-09-28 17:45
 */
@SpringBootApplication
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@Slf4j
public class ServiceProductApplication {



    public static void main(String[] args) {
            SpringApplication.run(ServiceProductApplication.class,args);
            log.info("启动成功");
    }

}
