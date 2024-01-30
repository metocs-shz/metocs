package com.metocs.common.oauth.mapper;

import com.metocs.common.datasource.mapper.CommonMapper;
import com.metocs.common.oauth.model.Oauth2Client;
import org.apache.ibatis.annotations.Select;

/**
 * @author metocs
 * @date 2024/1/30 23:28
 */
public interface Oauth2ClientMapper extends CommonMapper<Oauth2Client> {

    @Select("select id,client_id,client_id_issued_at,client_secret,client_secret_expires_at, " +
            "client_name,client_authentication_methods,authorization_grant_types,redirect_uris," +
            "postLogout_redirect_uris,scopes,client_settings,token_settings FROM oauth2_client where client_id = #{clientId}")
    Oauth2Client findByClientId(String clientId);

}

