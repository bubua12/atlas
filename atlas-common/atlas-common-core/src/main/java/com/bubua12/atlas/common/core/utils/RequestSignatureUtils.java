package com.bubua12.atlas.common.core.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 内部请求签名工具。
 */
public final class RequestSignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private RequestSignatureUtils() {
    }

    /**
     * 对请求关键信息进行 HMAC-SHA256 签名。
     */
    public static String sign(String secret, String callerService, String timestamp, String authorization,
                              String userId, String username, String encodedLoginUser) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signed = mac.doFinal(buildPayload(callerService, timestamp, authorization, userId, username,
                    encodedLoginUser).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signed);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("生成请求签名失败", e);
        }
    }

    /**
     * 常量时间比较签名，避免直接 equals。
     */
    public static boolean matches(String expectedSignature, String actualSignature) {
        if (expectedSignature == null || actualSignature == null) {
            return false;
        }
        return MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildPayload(String callerService, String timestamp, String authorization,
                                       String userId, String username, String encodedLoginUser) {
        return normalize(callerService) + '\n'
                + normalize(timestamp) + '\n'
                + normalize(authorization) + '\n'
                + normalize(userId) + '\n'
                + normalize(username) + '\n'
                + normalize(encodedLoginUser);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
