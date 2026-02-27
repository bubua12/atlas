package com.bubua12.atlas.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.bubua12.atlas.system.mapper")
public class AtlasSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasSystemApplication.class, args);
    }
}
