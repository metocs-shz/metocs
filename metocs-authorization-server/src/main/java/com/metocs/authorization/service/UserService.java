package com.metocs.authorization.service;

import com.metocs.common.oauth.mapper.UserAccountMapper;
import com.metocs.common.oauth.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * @author metocs
 * @date 2024/1/21 14:10
 */

@Component
public class UserService implements UserDetailsService {

    @Autowired
    private UserAccountMapper userAccountMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserAccount userAccount = userAccountMapper.loadUserByUsername(username);

        if (userAccount == null){
            throw new UsernameNotFoundException("用户名密码错误！");
        }

        //获取权限信息
        userAccount.setAuthorities(new HashSet<>());

        return userAccount;
    }

}
