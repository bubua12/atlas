package com.bubua12.atlas.gateway;

import com.bubua12.atlas.gateway.config.GatewayWhitelistProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Atlas Gateway Application
 */
@SpringBootApplication
@EnableConfigurationProperties(GatewayWhitelistProperties.class)
public class AtlasGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasGatewayApplication.class, args);
    }
}
