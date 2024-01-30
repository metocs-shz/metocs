package com.metocs.authorization.provider;

import com.metocs.authorization.token.SmsCodeAuthenticationToken;
import com.metocs.common.cache.RedisService;
import com.metocs.common.core.constant.BaseConstant;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;

/**
 * @author metocs
 * @date 2024/1/21 14:24
 */
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    private RedisService redisService;

    public SmsCodeAuthenticationProvider(UserDetailsService userDetailsService, RedisService redisService) {
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;

        String mobile = (String) authenticationToken.getPrincipal();
        Object code =  authenticationToken.getCredentials();

        UserDetails userDetails = userDetailsService.loadUserByUsername(mobile);

        checkCode(mobile,code.toString());

        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(userDetails,code, userDetails.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        authenticationResult.eraseCredentials();

        //清理验证码
        redisService.del(BaseConstant.SMS_TOKEN + mobile);
        return authenticationResult;
    }

    private void checkCode(String mobile,String code) {
        String data = (String) this.redisService.get(BaseConstant.SMS_TOKEN + mobile);
        if (!StringUtils.hasText(data)){
            throw new AuthenticationServiceException("验证码已过期！");
        }

        if (!code.equals(data)){
            throw new AuthenticationServiceException("验证码错误！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
