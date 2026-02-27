package com.bubua12.atlas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * fixme 引入了通用组件，自动引入配置
 */
@SpringBootApplication(scanBasePackages = {"com.bubua12.atlas.auth", "com.bubua12.atlas.common.redis"})
@EnableFeignClients
@EnableDiscoveryClient
public class AtlasAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasAuthApplication.class, args);
    }
}
