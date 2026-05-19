package com.ruoyi.bridge.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP请求工具类
 * 基于Spring RestTemplate实现，用于与渠道SDK服务端通信
 *
 * @author ruoyi
 */
@Component
public class BridgeHttpHelper {

    private static final Logger log = LoggerFactory.getLogger(BridgeHttpHelper.class);

    private final RestTemplate restTemplate;

    public BridgeHttpHelper() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 执行GET请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应字符串
     */
    public String doGet(String url, Map<String, String> params) {
        try {
            String fullUrl = convertUrl(url, params);
            log.debug("GET request: {}", fullUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("GET请求失败：url={}", url, e);
            throw new RuntimeException("HTTP GET failed: " + url, e);
        }
    }

    /**
     * 执行POST表单请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应字符串
     */
    public String doPost(String url, Map<String, String> params) {
        try {
            log.debug("POST request: url={}, params={}", url, params);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("POST请求失败：url={}", url, e);
            throw new RuntimeException("HTTP POST failed: " + url, e);
        }
    }

    /**
     * 执行POST JSON请求
     *
     * @param url      请求地址
     * @param jsonBody JSON字符串
     * @return 响应字符串
     */
    public String doPostJson(String url, String jsonBody) {
        try {
            log.debug("POST JSON request: url={}, body={}", url, jsonBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("POST JSON请求失败：url={}", url, e);
            throw new RuntimeException("HTTP POST JSON failed: " + url, e);
        }
    }

    /**
     * 将参数拼接到URL上
     *
     * @param url    基础URL
     * @param params 参数
     * @return 拼接后的完整URL
     */
    public String convertUrl(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        params.forEach(builder::queryParam);
        return builder.build().toUriString();
    }
}
