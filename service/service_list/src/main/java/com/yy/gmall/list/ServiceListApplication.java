package com.yy.gmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yu
 * @create 2021-10-12 19:59
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.yy.gmall"})
public class ServiceListApplication {
    public static void main(String[] args)
        {
            SpringApplication.run(ServiceListApplication.class,args);
        }

}
