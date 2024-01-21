package com.metocs.common.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metocs.common.core.response.ResponseEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author metocs
 * @date 2024/1/21 13:42
 */
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String traceId = request.getHeader("Trace_Id");
        response.setContentType("application/json;charset=utf-8");
        ObjectMapper objectMapper = new ObjectMapper();
        String resBody = "";
        resBody = objectMapper.writeValueAsString(ResponseEnum.NOT_ACCESS);
        this.logger.error("认证失败！{} API: {} 错误信息 {}",traceId,request.getRequestURI(),authException.getMessage());
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
    }
}
