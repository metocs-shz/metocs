package com.metocs.authorization.filter;

import com.metocs.authorization.handler.MyAuthenticationFailureHandler;
import com.metocs.authorization.handler.MySuccessHandler;
import com.metocs.authorization.provider.MyDaoAuthenticationProvider;
import com.metocs.common.core.utils.IpUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author metocs
 * @date 2024/1/21 14:12
 */
public class CaptchaUsernamePasswordFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/captcha/login", "POST");

    private static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";

    private static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    private static final String SPRING_SECURITY_FORM_CODE_KEY = "code";

    private static final String SPRING_SECURITY_FORM_SESSION_CODE_KEY = "captcha";



    public CaptchaUsernamePasswordFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    public CaptchaUsernamePasswordFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        MyDaoAuthenticationProvider myDaoAuthenticationProvider = new MyDaoAuthenticationProvider();
        ProviderManager providerManager = new ProviderManager(myDaoAuthenticationProvider);
        this.setAuthenticationSuccessHandler(new MySuccessHandler());
        this.setAuthenticationFailureHandler(new MyAuthenticationFailureHandler());
        this.setAuthenticationManager(providerManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // 需要是 POST 请求
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username = request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
        if (!StringUtils.hasText(username)){
            throw new AuthenticationServiceException("请输用户名！");
        }
        String password = request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
        if (!StringUtils.hasText(password)){
            throw new AuthenticationServiceException("请输入密码！");
        }

        // 获得请求验证码值
        String code = request.getParameter(SPRING_SECURITY_FORM_CODE_KEY);
        if (!StringUtils.hasText(code)){
            throw new AuthenticationServiceException("请输入验证码!");
        }

        HttpSession session = request.getSession();
        String sessionVerifyCode = (String) session.getAttribute(SPRING_SECURITY_FORM_SESSION_CODE_KEY);
        if(!StringUtils.hasText(sessionVerifyCode)){
            throw new AuthenticationServiceException("请重新申请验证码!");
        }


        if (!sessionVerifyCode.equalsIgnoreCase(code)) {
            throw new AuthenticationServiceException("验证码错误!");
        }
        // 验证码验证成功，清除 session 中的验证码
        session.removeAttribute(SPRING_SECURITY_FORM_SESSION_CODE_KEY);


        UsernamePasswordAuthenticationToken captchaUsernamePasswordToken = UsernamePasswordAuthenticationToken.unauthenticated(username,password);
        setDetails(request,captchaUsernamePasswordToken);
        return this.getAuthenticationManager().authenticate(captchaUsernamePasswordToken);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        WebAuthenticationDetails webAuthenticationDetails =
                new WebAuthenticationDetails(IpUtils.getIpAddr(request),request.getRequestedSessionId());
        authRequest.setDetails(webAuthenticationDetails);
    }
}
