package com.bubua12.atlas.common.core.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具类
 */
public class PasswordUtils {

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
