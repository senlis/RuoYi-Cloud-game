package com.ruoyi.common.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 签名工具类
 * <p>
 * 提供 MD5 签名功能，用于后端服务间通信的请求签名。
 * 签名规则：参数按 key 首字母排序，拼接为 key1+value1+key2+value2+...+secretKey，取 MD5。
 *
 * @author ruoyi
 */
public class SignUtils {

    /**
     * 计算 MD5 签名
     *
     * @param params   请求参数字典（key-value）
     * @param secretKey 签名密钥
     * @return MD5 签名（32位小写十六进制）
     */
    public static String md5Sign(Map<String, String> params, String secretKey) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        // 按 key 首字母排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        // 拼接 key1+value1+key2+value2+...+secretKey
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            sb.append(key).append(value != null ? value : "");
        }
        if (secretKey != null) {
            sb.append(secretKey);
        }
        return md5(sb.toString());
    }

    /**
     * MD5 哈希
     *
     * @param str 待哈希字符串
     * @return 32位小写十六进制 MD5
     */
    public static String md5(String str) {
        if (str == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }
}
