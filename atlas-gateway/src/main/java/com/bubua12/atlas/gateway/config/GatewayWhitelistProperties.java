package com.bubua12.atlas.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "atlas.gateway")
public class GatewayWhitelistProperties {

    private List<String> whitelist = new ArrayList<>();
}
