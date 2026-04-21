package com.bubua12.atlas.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全相关配置。
 *
 * <p>gateway 和 internal 分开配置，是为了显式区分：
 * 网关代表“用户请求已经被认证过”；
 * internal 代表“当前 HTTP 请求来自受信任的内部服务”。
 */
@Data
@ConfigurationProperties(prefix = "atlas.security")
public class AtlasSecurityProperties {

    private Gateway gateway = new Gateway();

    private Internal internal = new Internal();

    @Data
    public static class Gateway {

        /**
         * 网关签发用户断言时使用的签名密钥。
         */
        private String secret = "change-me-gateway-signature-secret";

        /**
         * 允许请求时间戳与服务端时间的最大偏差，单位秒。
         */
        private long allowedSkewSeconds = 300;

    }

    @Data
    public static class Internal {

        /**
         * 当前服务对外声明的服务名，Feign 调用时会写入请求头。
         */
        private String serviceName;

        /**
         * 当前服务给别人发起内部调用时使用的签名密钥。
         */
        private String currentSecret;

        private long allowedSkewSeconds = 300;

        /**
         * 受信任的内部服务密钥映射，key 为服务名，value 为对应签名密钥。
         */
        private Map<String, String> trustedServices = new HashMap<>();
    }
}
