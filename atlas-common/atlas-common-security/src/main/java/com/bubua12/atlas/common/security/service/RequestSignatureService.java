package com.bubua12.atlas.common.security.service;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.GatewayUserContext;
import com.bubua12.atlas.common.security.config.AtlasSecurityProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 请求签名与验签服务。
 *
 * <p>这里统一封装了两套协议：
 * 1. gateway：证明“当前用户身份是网关验过 token 后签发的”；
 * 2. internal：证明“当前 HTTP 请求来自某个受信任的内部服务”。
 * 两套协议现在都只保留“时间窗口 + 签名”校验，避免把 Redis 变成认证链路上的强依赖。
 */
@Slf4j
@RequiredArgsConstructor
public class RequestSignatureService {

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final AtlasSecurityProperties securityProperties;

    /**
     * 统一生成毫秒级时间戳，便于调用方和验签方共用同一个窗口判断。
     */
    public String currentTimestamp() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

    /**
     * 网关签名载荷用 URL safe Base64，避免经过代理或日志时被额外转义。
     */
    public String encodeGatewayUserContext(GatewayUserContext userContext) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(userContext);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (JsonProcessingException e) {
            throw new BusinessException("序列化网关身份信息失败", e, BusinessErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 网关签名的用户断言必须先完成验签，再允许反序列化成业务上下文。
     */
    public GatewayUserContext decodeGatewayUserContext(String payload) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readValue(bytes, GatewayUserContext.class);
        } catch (Exception e) {
            throw new BusinessException("Failed to parse gateway user context", e, BusinessErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 对网关请求进行签名
     *
     * @param method    method+path、确保签名不能脱离原始请求上下文复用
     * @param path      method+path、确保签名不能脱离原始请求上下文复用
     * @param payload   确保用户身份内容不能被改
     * @param timestamp 确保请求不是无限期可复用
     * @return hmacSha256签名
     */
    public String signGatewayRequest(String method, String path, String payload, String timestamp) {
        String secret = securityProperties.getGateway().getSecret();
        assertSecret(secret, "网关签名密钥不存在");
        return hmacSha256(secret, canonicalGatewayData(method, path, payload, timestamp));
    }

    /**
     * 网关验签成功后，返回下游真正可以信任的用户上下文。
     */
    public GatewayUserContext verifyGatewayRequest(String method, String path, String payload, String timestamp, String signature) {
        validateTimestamp(timestamp, securityProperties.getGateway().getAllowedSkewSeconds(),
                "网关请求时间非法或请求已过期");

        // 使用网关一致的签名计算算法，计算后进行验证
        String expected = signGatewayRequest(method, path, payload, timestamp);
        if (signCompareFail(expected, signature)) {
            throw new BusinessException("下游请求网关签名验证失败", BusinessErrorCode.UNAUTHORIZED);
        }

        return decodeGatewayUserContext(payload);
    }

    public String signInternalRequest(String method, String path, String callerService, String timestamp) {
        String secret = securityProperties.getInternal().getCurrentSecret();
        assertSecret(secret, "Internal caller secret is missing");
        if (!StringUtils.hasText(callerService)) {
            throw new BusinessException("Internal caller service name is missing", BusinessErrorCode.SYSTEM_ERROR);
        }
        return hmacSha256(secret, canonicalInternalData(method, path, callerService, timestamp));
    }

    /**
     * 内部服务认证只证明“谁在调我”，不承载用户身份，所以返回的是调用方服务名。
     */
    public String verifyInternalRequest(
            String method,
            String path,
            String callerService,
            String timestamp,
            String signature
    ) {
        if (!StringUtils.hasText(callerService)) {
            throw new BusinessException("[atlas-security]内部调用服务名不存在", BusinessErrorCode.FORBIDDEN);
        }

        String trustedSecret = securityProperties.getInternal().getTrustedServices().get(callerService);
        if (!StringUtils.hasText(trustedSecret)) {
            throw new BusinessException("[atlas-security] 不信任的内部服务调用：" + callerService, BusinessErrorCode.FORBIDDEN);
        }

        validateTimestamp(timestamp, securityProperties.getInternal().getAllowedSkewSeconds(),
                "[atlas-security] 内部请求时间过期或非法，请重试");

        String expected = hmacSha256(trustedSecret, canonicalInternalData(method, path, callerService, timestamp));
        if (signCompareFail(expected, signature)) {
            throw new BusinessException("[atlas-security] 内部服务调用签名校验失败", BusinessErrorCode.FORBIDDEN);
        }

        return callerService;
    }

    /**
     * 验证请求时间窗口
     *
     * @param timestamp
     * @param allowedSkewSeconds
     * @param message
     */
    private void validateTimestamp(String timestamp, long allowedSkewSeconds, String message) {
        try {
            long requestMillis = Long.parseLong(timestamp);
            long delta = Math.abs(System.currentTimeMillis() - requestMillis);
            if (delta > allowedSkewSeconds * 1000) {
                throw new BusinessException(message, BusinessErrorCode.UNAUTHORIZED);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(message, e, BusinessErrorCode.UNAUTHORIZED);
        }
    }

    private void assertSecret(String secret, String message) {
        if (!StringUtils.hasText(secret)) {
            throw new BusinessException(message, BusinessErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * method + path + payload + timestamp 必须保持完全一致，任何一项被篡改都会导致验签失败。
     */
    private String canonicalGatewayData(String method, String path, String payload, String timestamp) {
        return normalizeMethod(method) + "\n"
                + normalizePath(path) + "\n"
                + payload + "\n"
                + timestamp;
    }

    /**
     * 内部调用签名里不放用户数据，只放调用方服务名，避免把两类身份语义混用。
     */
    private String canonicalInternalData(String method, String path, String callerService, String timestamp) {
        return normalizeMethod(method) + "\n"
                + normalizePath(path) + "\n"
                + callerService + "\n"
                + timestamp;
    }

    private String normalizeMethod(String method) {
        return method == null ? "GET" : method.trim().toUpperCase();
    }

    /**
     * 查询参数不参与签名，避免因为参数顺序或网关转发差异导致合法请求误验签失败。
     */
    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        int queryIndex = path.indexOf('?');
        return queryIndex >= 0 ? path.substring(0, queryIndex) : path;
    }

    /**
     * 签名比较使用常量时间算法，避免把比较过程变成侧信道。
     */
    private boolean signCompareFail(String expected, String actual) {
        if (expected == null || actual == null) {
            return true;
        }
        return !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 统一的 HMAC-SHA256 实现，确保 gateway/internal 两条链路的签名算法保持一致。
     */
    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new BusinessException("[atlas-security] 计算HMAC-SHA256签名失败", e, BusinessErrorCode.SYSTEM_ERROR);
        }
    }
}
