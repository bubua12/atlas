package com.bubua12.atlas.common.core.utils;

import com.bubua12.atlas.common.core.constant.Constants;

/**
 * Token 处理工具类。
 */
public final class TokenUtils {

    private TokenUtils() {
    }

    /**
     * 解析 Authorization 请求头中的原始 token。
     *
     * @param token token 请求头或原始 token
     * @return 原始 token，不存在时返回 null
     */
    public static String resolveToken(String token) {
        if (token == null) {
            return null;
        }

        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.regionMatches(true, 0, Constants.TOKEN_PREFIX, 0, Constants.TOKEN_PREFIX.length())) {
            String rawToken = trimmed.substring(Constants.TOKEN_PREFIX.length()).trim();
            return rawToken.isEmpty() ? null : rawToken;
        }

        return trimmed;
    }

    /**
     * 统一追加 Bearer 前缀。
     *
     * @param token 原始 token
     * @return 标准 Authorization 请求头值
     */
    public static String withBearerPrefix(String token) {
        String rawToken = resolveToken(token);
        if (rawToken == null) {
            return null;
        }
        return Constants.TOKEN_PREFIX + rawToken;
    }
}
