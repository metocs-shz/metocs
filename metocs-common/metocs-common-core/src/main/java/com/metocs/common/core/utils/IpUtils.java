package com.metocs.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

/**
 * @author metocs
 * @date 2024/1/21 14:16
 */
public class IpUtils {

    public static String getIp(ServerWebExchange serverWebExchange){
        ServerHttpRequest request = serverWebExchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
        }
        return ip.replaceAll(":", ".");
    }


    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");

        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader("WL-Proxy-Client-IP");

        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getRemoteAddr();

        }
        if(ip.trim().contains(",")){//为什么会有这一步，因为经过多层代理后会有多个代理，取第一个ip地址就可以了
            String [] ips=ip.split(",");
            ip=ips[0];
        }

        return ip;
    }

    /**
     * 获取操作系统,浏览器及浏览器版本信息
     * 废弃
     * @param request http请求
     */
    @Deprecated
    public static String getOsAndBrowserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String user = userAgent.toLowerCase();
        String os;

        //=================OS Info=======================
        if (user.contains("windows")) {
            os = "windows";
        } else if (user.contains("mac")) {
            os = "mac";
        } else if (user.contains("x11")) {
            os = "x11";
        } else if (user.contains("android")) {
            os = "android";
        } else if (user.contains("iphone")) {
            os = "iphone";
        }else if (user.contains("ipad")){
            os = "ipad";
        }else {
            os = null;
        }
        return os;
    }
}
