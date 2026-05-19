package com.ruoyi.bridge.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加密工具类
 * 提供MD5、RSA验签、HMAC-SHA1等常用加密方法
 *
 * @author ruoyi
 */
public class CryptoUtil {

    private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);

    private CryptoUtil() {
        // 工具类禁止实例化
    }

    /**
     * MD5哈希（拼接所有参数后计算）
     *
     * @param parts 待拼接的参数
     * @return 32位小写MD5
     */
    public static String md5(String... parts) {
        try {
            String content = Arrays.stream(parts)
                    .filter(s -> s != null)
                    .collect(Collectors.joining());
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5计算失败", e);
            throw new RuntimeException("MD5 computation failed", e);
        }
    }

    /**
     * RSA签名验证
     *
     * @param content   待验签内容
     * @param sign      签名值（Base64编码）
     * @param publicKey 公钥（Base64编码的X.509格式）
     * @return 验签是否通过
     */
    public static boolean rsaVerify(String content, String sign, String publicKey) {
        try {
            // 解码公钥
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 验签
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            byte[] signBytes = Base64.getDecoder().decode(sign);
            return signature.verify(signBytes);
        } catch (Exception e) {
            log.error("RSA验签失败", e);
            return false;
        }
    }

    /**
     * Base64编码
     *
     * @param data 待编码字符串
     * @return Base64编码结果
     */
    public static String base64Encode(String data) {
        if (data == null) return "";
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * HMAC-SHA1签名
     *
     * @param data 待签名数据
     * @param key  密钥
     * @return HMAC-SHA1签名结果（小写十六进制字符串）
     */
    public static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("HMAC-SHA1计算失败", e);
            throw new RuntimeException("HMAC-SHA1 computation failed", e);
        }
    }
}
