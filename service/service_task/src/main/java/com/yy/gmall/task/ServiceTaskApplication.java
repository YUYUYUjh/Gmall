package com.yy.gmall.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        exclude = DataSourceAutoConfiguration.class,
        scanBasePackages = {"com.yy.gmall"})//取消数据源自动配置
@EnableDiscoveryClient
@EnableScheduling
public class ServiceTaskApplication {
 
    public static void main(String[] args) {
        SpringApplication.run(ServiceTaskApplication.class);
    }
}