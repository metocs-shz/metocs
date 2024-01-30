package com.metocs.authorization.provider;

import com.metocs.common.core.exception.CommonException;
import com.metocs.common.core.utils.RsaUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author metocs
 * @date 2024/1/21 16:38
 */
public class MyDaoAuthenticationProvider extends DaoAuthenticationProvider {


    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {
            this.logger.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码不能为空！"));
        }

        String presentedPassword = authentication.getCredentials().toString();
        String password = "";
        try {
            password = RsaUtils.decryptByPrivateKey(presentedPassword);
        }catch (CommonException e){
            throw new BadCredentialsException("数据解密失败！");
        }


        if (!super.getPasswordEncoder().matches(password, userDetails.getPassword())) {
            this.logger.debug("用户名密码验证失败 password: "+userDetails.getPassword() +"  userPass:  "+password);

            Object oauthUser = (Object) userDetails;

            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "用户名密码错误！"));
        }
    }
}
