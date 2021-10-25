package com.yy.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yu
 * @create 2021-10-16 12:39
 */
@SpringBootApplication
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
public class ServiceUserApplication {
    public static void main(String[] args)
        {
            SpringApplication.run(ServiceUserApplication.class,args);
        }

}
