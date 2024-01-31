package com.metocs.authorization.filter;

import com.metocs.authorization.handler.MyAuthenticationFailureHandler;
import com.metocs.authorization.handler.MySuccessHandler;
import com.metocs.authorization.token.SmsCodeAuthenticationToken;
import com.metocs.common.core.utils.IpUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author metocs
 * @date 2024/1/21 14:20
 */
public class SmsAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/mobile/login", "POST");

    public static final String SPRING_SECURITY_FORM_PHONE_KEY = "phone";

    public static final String SPRING_SECURITY_FORM_CODE_KEY = "code";

    public SmsAuthenticationProcessingFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);

    }

    public SmsAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.setAuthenticationSuccessHandler(new MySuccessHandler());
        this.setAuthenticationFailureHandler(new MyAuthenticationFailureHandler());
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String phone = request.getParameter(SPRING_SECURITY_FORM_PHONE_KEY);
        if (!StringUtils.hasText(phone)){
            throw new AuthenticationServiceException("请输入手机号码！");
        }
        String code = request.getParameter(SPRING_SECURITY_FORM_CODE_KEY);
        if (!StringUtils.hasText(code)){
            throw new AuthenticationServiceException("请输入验证码！");
        }

        SmsCodeAuthenticationToken smsCodeAuthenticationToken = new SmsCodeAuthenticationToken(phone,code);
        setDetails(request,smsCodeAuthenticationToken);
        return this.getAuthenticationManager().authenticate(smsCodeAuthenticationToken);
    }

    protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
        WebAuthenticationDetails webAuthenticationDetails =
                new WebAuthenticationDetails(IpUtils.getIpAddr(request),request.getRequestedSessionId());
        authRequest.setDetails(webAuthenticationDetails);
    }
}
