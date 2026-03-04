package com.bubua12.atlas.infra;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 基础设施服务启动类（代码生成、文件管理等）
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.bubua12.atlas.infra.mapper")
public class AtlasInfraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasInfraApplication.class, args);
    }
}
