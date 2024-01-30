package com.metocs.common.oauth.introspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.metocs.common.oauth.model.OauthUserAuthenticate;
import com.metocs.common.oauth.service.RedisOAuth2AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author metocs
 * @date 2024/1/27 12:48
 */
public class RedisOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final static Logger logger = LoggerFactory.getLogger(RedisOpaqueTokenIntrospector.class);

    private RedisOAuth2AuthorizationService redisOAuth2AuthorizationService;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {

        OAuth2Authorization oAuth2Authorization = redisOAuth2AuthorizationService.findByToken(token, null);

        if (oAuth2Authorization == null){

            logger.debug("未找到token存储信息 token ： {}",token);

            throw new BadOpaqueTokenException("Provided token isn't active");
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = oAuth2Authorization.getAccessToken();

        if (!accessToken.isActive()){

            //进一步验证是否是因为token未生效导致

            if (accessToken.isInvalidated() || accessToken.isExpired()){

                logger.debug("token 信息已经失效  {}", JSON.toJSONString(accessToken));

                throw new BadOpaqueTokenException("Provided token isn't active");
            }

            if (accessToken.isBeforeUse()){
                logger.debug("token 信息未生效 {}",JSON.toJSONString(accessToken));
            }

            if (!CollectionUtils.isEmpty(accessToken.getClaims())) {
                Instant notBefore = (Instant) accessToken.getClaims().get("nbf");
                Instant plus = Instant.now().plus(10, ChronoUnit.MINUTES);
                boolean before = plus.isBefore(notBefore);
                if (before){
                    throw new BadOpaqueTokenException("Token 时间参数错误！服务器时间超时10分钟");
                }
            }else {
                throw new BadOpaqueTokenException("Token 时间参数错误！");
            }
        }

        Authentication attribute = oAuth2Authorization.getAttribute("java.security.Principal");
        if (attribute == null ){

            logger.debug("token 存储内容错误  {}",JSON.toJSONString(oAuth2Authorization));

            throw new BadOpaqueTokenException("Provided token isn't active");
        }
        Object principal = attribute.getPrincipal();
        HashMap<String,Object> hashMap = JSON.parseObject(JSON.toJSONString(principal), new TypeReference<>(){});

        hashMap.remove("authorities");
        hashMap.remove("password");

        Set<String> authorizedScopes = oAuth2Authorization.getAuthorizedScopes();
        ArrayList<GrantedAuthority> objects = new ArrayList<>();
        // 添加鉴权链接
        for (Object authority : attribute.getAuthorities()) {
            objects.add(new SimpleGrantedAuthority(authority.toString()));
        }
        //添加授权范围
        for (String authorizedScope : authorizedScopes) {
            objects.add(new SimpleGrantedAuthority(authorizedScope));
        }

        return new OauthUserAuthenticate(hashMap,objects,"oauthUser");
    }
}
