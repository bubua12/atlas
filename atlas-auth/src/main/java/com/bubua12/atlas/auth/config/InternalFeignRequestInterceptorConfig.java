package com.bubua12.atlas.auth.config;

import com.bubua12.atlas.common.core.constant.RequestHeaderConstants;
import com.bubua12.atlas.common.security.config.AtlasSecurityProperties;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为内部 Feign 调用补充服务身份签名。
 *
 * <p>这里只处理“服务到服务”的调用，不透传用户身份。
 * 像登录场景里 auth 调 system 查用户时，依赖的就是这条链。
 */
@Configuration
public class InternalFeignRequestInterceptorConfig {

    @Bean
    public RequestInterceptor internalRequestSignatureInterceptor(
            AtlasSecurityProperties securityProperties,
            RequestSignatureService requestSignatureService
    ) {
        return requestTemplate -> applyInternalSignature(requestTemplate, securityProperties, requestSignatureService);
    }

    private void applyInternalSignature(
            RequestTemplate requestTemplate,
            AtlasSecurityProperties securityProperties,
            RequestSignatureService requestSignatureService
    ) {
        String serviceName = securityProperties.getInternal().getServiceName();
        String currentSecret = securityProperties.getInternal().getCurrentSecret();
        if (serviceName == null || serviceName.isBlank() || currentSecret == null || currentSecret.isBlank()) {
            // 没配内部服务认证时保持静默，避免本地开发阶段直接把所有 Feign 都拦死。
            return;
        }

        String timestamp = requestSignatureService.currentTimestamp();
        String nonce = requestSignatureService.newNonce();
        String path = requestTemplate.path();
        // 签名使用 Feign 实际要请求的 path，保证接收方验签时看到的是同一份 canonical 数据。
        String signature = requestSignatureService.signInternalRequest(
                requestTemplate.method(),
                path,
                serviceName,
                timestamp,
                nonce
        );

        requestTemplate.header(RequestHeaderConstants.X_INTERNAL_SERVICE, serviceName);
        requestTemplate.header(RequestHeaderConstants.X_INTERNAL_TIMESTAMP, timestamp);
        requestTemplate.header(RequestHeaderConstants.X_INTERNAL_NONCE, nonce);
        requestTemplate.header(RequestHeaderConstants.X_INTERNAL_SIGNATURE, signature);
    }
}
