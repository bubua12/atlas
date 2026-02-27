package com.bubua12.atlas.infra;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.bubua12.atlas.infra.mapper")
public class AtlasInfraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasInfraApplication.class, args);
    }
}
