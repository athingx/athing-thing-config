package com.github.athingx.athing.aliyun.config.component.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

    /**
     * 字节数组转16进制字符串
     *
     * @param bArray 目标字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(final byte[] bArray) {
        final StringBuilder sb = new StringBuilder(bArray.length * 2);
        for (byte b : bArray)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    /**
     * 获取字符串的SHA256签名
     *
     * @param string 目标字符串
     * @return SHA256签名
     */
    public static String signBySHA256(String string) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException cause) {
            throw new RuntimeException(cause);
        }
    }

}
