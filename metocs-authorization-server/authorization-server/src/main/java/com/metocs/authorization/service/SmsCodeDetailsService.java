package com.metocs.authorization.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author metocs
 * @date 2024/1/21 14:11
 */
public class SmsCodeDetailsService implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return null;
    }
}
