package com.ruoyi.gserve.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * SecureKey 签名验证服务
 * <p>
 * 验证游戏服上报请求的签名合法性。
 * 签名算法：HMAC-SHA256(签名原文, SecureKey) 或 MD5(签名原文 + SecureKey)
 * <p>
 * 签名原文 = HTTP_METHOD + "\n" + URI_PATH + "\n" + Timestamp + "\n" + Body_MD5
 *
 * @author ruoyi
 */
@Service
public class SecureKeyService {

    private static final Logger log = LoggerFactory.getLogger(SecureKeyService.class);

    /**
     * 验证签名
     *
     * @param secureKey   渠道 SecureKey
     * @param signContent 签名原文
     * @param signature   请求携带的签名
     * @param signType    签名类型（md5 / hmac-sha256）
     * @return true 验证通过
     */
    public boolean verify(String secureKey, String signContent, String signature, String signType) {
        if (secureKey == null || signContent == null || signature == null) {
            return false;
        }

        try {
            String expectedSign;
            if ("hmac-sha256".equalsIgnoreCase(signType)) {
                expectedSign = hmacSha256(signContent, secureKey);
            } else {
                // 默认 MD5 方式
                expectedSign = md5(signContent + secureKey);
            }

            // 恒定时间比较，防时序攻击
            return MessageDigest.isEqual(expectedSign.getBytes(), signature.getBytes());
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 构建签名原文
     */
    public String buildSignContent(String method, String path, String timestamp, String bodyMd5) {
        return method + "\n" + path + "\n" + timestamp + "\n" + (bodyMd5 != null ? bodyMd5 : "");
    }

    /**
     * 计算 Body MD5
     */
    public String md5Body(byte[] body) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(body);
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }

    /**
     * HMAC-SHA256 签名
     */
    public String hmacSha256(String data, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        return bytesToHex(mac.doFinal(data.getBytes()));
    }

    /**
     * MD5 签名
     */
    public String md5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return bytesToHex(md.digest(data.getBytes()));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
