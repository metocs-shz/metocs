package com.metocs.authorization.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metocs.common.core.response.ResponseData;
import com.metocs.common.core.response.ResponseEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author metocs
 * @date 2024/1/21 16:49
 */
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        String resBody = "";
        if (exception instanceof BadCredentialsException){
            resBody = objectMapper.writeValueAsString(ResponseData.fail(ResponseEnum.NOT_ACCESS,exception.getMessage()));
        }else {
            resBody = objectMapper.writeValueAsString(ResponseData.fail(ResponseEnum.FAIL,exception.getMessage()));
        }
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
        out.flush();
        out.close();
    }
}
