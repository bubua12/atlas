package com.bubua12.atlas.auth.config;

import com.bubua12.atlas.common.core.constant.SecurityHeaderConstants;
import com.bubua12.atlas.common.core.utils.RequestSignatureUtils;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 内部调用安全配置。
 */
@Configuration
public class FeignSecurityConfig {

    @Bean
    public RequestInterceptor internalRequestInterceptor(
            @Value("${spring.application.name}") String applicationName,
            @Value("${atlas.security.request-signature.secret:${ATLAS_REQUEST_SIGNATURE_SECRET:atlas-request-signature-secret-change-me}}")
            String requestSignatureSecret) {
        return template -> {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = RequestSignatureUtils.sign(requestSignatureSecret, applicationName, timestamp,
                    null, null, null, null);
            template.header(SecurityHeaderConstants.CALLER_SERVICE, applicationName);
            template.header(SecurityHeaderConstants.REQUEST_TIMESTAMP, timestamp);
            template.header(SecurityHeaderConstants.REQUEST_SIGNATURE, signature);
        };
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 2);
    }
}
