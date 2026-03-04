package com.bubua12.atlas.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 监控服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
public class AtlasMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasMonitorApplication.class, args);
    }
}
