package com.yy.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * exclude=DataSourceAutoConfiguration.class 说明:
 *      因为在父工程引入了mybatisPlus启动器,所以在服务启动时会检查数据源连接
 *      但是在配置文件并没有配置数据源
 *      所以需要取消数据源自动配置
 *      否则会报错
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.yy.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.yy.gmall"})
public class ServiceItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceItemApplication.class, args);
    }

}