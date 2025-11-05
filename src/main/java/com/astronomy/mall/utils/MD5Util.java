package com.astronomy.mall.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * MD5加密工具类
 */
public class MD5Util {

    /**
     * MD5加密
     *
     * @param str 待加密字符串
     * @return 加密后的32位小写字符串
     */
    public static String encrypt(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证密码
     *
     * @param plainPassword 明文密码
     * @param encryptedPassword 加密后的密码
     * @return true-密码正确, false-密码错误
     */
    public static boolean verify(String plainPassword, String encryptedPassword) {
        String encrypted = encrypt(plainPassword);
        return encrypted != null && encrypted.equals(encryptedPassword);
    }
}