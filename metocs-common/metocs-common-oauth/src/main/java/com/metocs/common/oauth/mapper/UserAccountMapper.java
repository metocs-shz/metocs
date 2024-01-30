package com.metocs.common.oauth.mapper;

import com.metocs.common.datasource.mapper.CommonMapper;
import com.metocs.common.oauth.model.UserAccount;
import org.apache.ibatis.annotations.Select;

/**
 * @author metocs
 * @date 2024/1/30 23:42
 */
public interface UserAccountMapper extends CommonMapper<UserAccount> {

    @Select("SELECT id,name,phone,username,password,email,status,deleted,expire_time,create_time,update_time " +
            "FROM user_account where username = #{username} or phone = #{username}")
    UserAccount loadUserByUsername(String username);

}
