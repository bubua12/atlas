package com.bubua12.atlas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.bubua12.atlas")
@EnableFeignClients
@EnableDiscoveryClient
public class AtlasAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasAuthApplication.class, args);
    }
}
