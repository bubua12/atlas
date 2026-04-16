package com.bubua12.atlas.common.security.feign;

import com.bubua12.atlas.common.core.constant.RequestHeaderConstants;
import com.bubua12.atlas.common.security.config.AtlasSecurityProperties;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import feign.Capability;
import feign.Client;
import feign.Request;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 在 Feign 最终请求出站前补充内部服务签名。
 *
 * <p>这里不再基于 {@code RequestTemplate} 签名，而是包装最终 {@link Request}，
 * 确保类级别 {@code @FeignClient(path = ...)}、target url 等信息都已经合并完成。
 */
@Slf4j
public class InternalRequestSigningCapability implements Capability {

    private final AtlasSecurityProperties securityProperties;
    private final RequestSignatureService requestSignatureService;

    public InternalRequestSigningCapability(
            AtlasSecurityProperties securityProperties,
            RequestSignatureService requestSignatureService
    ) {
        this.securityProperties = securityProperties;
        this.requestSignatureService = requestSignatureService;
    }

    @Override
    public Client enrich(Client delegate) {
        return (request, options) -> delegate.execute(signRequest(request), options);
    }

    private Request signRequest(Request request) {
        String serviceName = securityProperties.getInternal().getServiceName();
        String currentSecret = securityProperties.getInternal().getCurrentSecret();
        if (!hasText(serviceName) || !hasText(currentSecret)) {
            return request;
        }

        String timestamp = requestSignatureService.currentTimestamp();
        String nonce = requestSignatureService.newNonce();
        String path = extractSigningPath(request.url());

        log.info("【请求方-携带签名】计算签名参数：method: {}，path: {}，service: {}，time: {}, nonce: {}",
                request.httpMethod().name(), path, serviceName, timestamp, nonce);

        String signature = requestSignatureService.signInternalRequest(
                request.httpMethod().name(),
                path,
                serviceName,
                timestamp,
                nonce
        );

        Map<String, Collection<String>> headers = new LinkedHashMap<>(request.headers());
        headers.put(RequestHeaderConstants.X_INTERNAL_SERVICE, List.of(serviceName));
        headers.put(RequestHeaderConstants.X_INTERNAL_TIMESTAMP, List.of(timestamp));
        headers.put(RequestHeaderConstants.X_INTERNAL_NONCE, List.of(nonce));
        headers.put(RequestHeaderConstants.X_INTERNAL_SIGNATURE, List.of(signature));

        return Request.create(
                request.httpMethod(),
                request.url(),
                headers,
                request.body(),
                request.charset(),
                request.requestTemplate()
        );
    }

    /**
     * Feign 最终 Request 上的 url 可能是绝对地址，也可能是相对路径，这里统一提取 path 部分参与签名。
     */
    private String extractSigningPath(String requestUrl) {
        if (!hasText(requestUrl)) {
            return "/";
        }

        URI uri = URI.create(requestUrl);
        String rawPath = uri.getRawPath();
        return hasText(rawPath) ? rawPath : "/";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
