package com.metocs.common.oauth.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metocs.common.cache.RedisService;
import com.metocs.common.oauth.model.RedisAuthorization;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@Component
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final static Logger logger = LoggerFactory.getLogger(RedisOAuth2AuthorizationService.class);

    @Autowired
    private RedisService redisService;

    private final static String ACCESS = "oauth:access:";

    private final static String INIT = "oauth:init:";

    private final static String STATE = "oauth:state:";

    private final static String CODE = "oauth:code:";

    private final static String REFRESH = "oauth:refresh_token:";

    private final static String TOKEN = "oauth:access_token:";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 初始化序列化配置
        ClassLoader classLoader = RedisOAuth2AuthorizationService.class.getClassLoader();
        // 加载security提供的Modules
        List<Module> modules = SecurityJackson2Modules.getModules(classLoader);
        MAPPER.registerModules(modules);
        // 加载Authorization Server提供的Module
        MAPPER.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }


    @Autowired
    private RegisteredClientRepository registeredClientRepository;


    private static boolean isComplete(OAuth2Authorization authorization) {
        return authorization.getAccessToken() != null;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "未获取到授权信息！");
        //转换授权对象
        RedisAuthorization redisAuthorization = toEntity(authorization);

        if (isComplete(authorization)){

            OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
            if (!accessToken.isActive()){
                //清除鉴权数据
                this.remove(authorization);
                return;
            }

            //清理所有数据
            OAuth2Authorization oAuth2Authorization = this.findById(authorization.getId());
            if (oAuth2Authorization!=null){
                this.clean(oAuth2Authorization);
            }

            Long expireToken = 18000L;

            OAuth2Authorization nowAuth = toObject(redisAuthorization);
            if (nowAuth.getAccessToken() !=null){
                expireToken = getExpireToken(nowAuth.getAccessToken());
            }

            if (nowAuth.getRefreshToken() !=null){
                expireToken = getExpireToken(nowAuth.getRefreshToken());
            }

            redisService.set(ACCESS+authorization.getId(),redisAuthorization,expireToken);
            saveIdComplete(authorization);
        }else {
            redisService.set(INIT+authorization.getId(),redisAuthorization,180L);
            saveIdInit(authorization);
        }

    }

    private void saveIdComplete(OAuth2Authorization authorization) {
        this.saveAccessToken(authorization);
        this.saveRefreshToken(authorization);
    }

    private void saveAccessToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                authorization.getToken(OAuth2AccessToken.class);

        if (accessToken != null){
            redisService.set(TOKEN+ DigestUtils.sha256Hex(accessToken.getToken().getTokenValue()),
                    authorization.getId(),getExpireToken(authorization.getAccessToken()));
        }
    }

    private void saveRefreshToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);
        if (refreshToken != null){
            redisService.set(REFRESH+refreshToken.getToken().getTokenValue(),
                    authorization.getId().trim(),getExpireToken(authorization.getRefreshToken()));
        }
    }


    private void saveIdInit(OAuth2Authorization authorization) {
        this.saveState(authorization);
        this.saveAuthorizationCode(authorization);
    }

    private void saveState(OAuth2Authorization authorization) {
        if (authorization.getAttributes() == null){
            return;
        }
        Object attribute = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (attribute != null){
            redisService.set(STATE+attribute,authorization.getId().trim(),180L);
        }
    }

    private void saveAuthorizationCode(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null){
            redisService.set(CODE+authorizationCode.getToken().getTokenValue(),authorization.getId(),180L);
        }
    }


    @Override
    public void remove(OAuth2Authorization authorization) {
        this.clean(authorization);
        redisService.del(ACCESS+authorization.getId());
    }

    private void clean(OAuth2Authorization authorization) {
        //STATE
        List<String> keys = new ArrayList<>();
        Object attribute = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (attribute!=null){
            keys.add(STATE+attribute);
        }
        //CODE
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null){
            keys.add(CODE+authorizationCode.getToken());
        }
        //INIT
        keys.add(INIT+authorization.getId());

        //TOKEN
        if (authorization.getAccessToken()!=null) {
            String accessToken = authorization.getAccessToken().getToken().getTokenValue();
            keys.add(TOKEN + DigestUtils.sha256Hex(accessToken));
        }

        //REFRESH
        if (authorization.getRefreshToken()!=null){
            String refreshToken = authorization.getRefreshToken().getToken().getTokenValue();
            keys.add(REFRESH+refreshToken);
        }
        redisService.del(keys.toArray(new String[]{}));
    }

    @Override
    public OAuth2Authorization findById(String id) {
        String access = (String) redisService.get(ACCESS + id);

        RedisAuthorization accessAuthorization = JSON.parseObject(access, RedisAuthorization.class);

        if (accessAuthorization != null){
            return toObject(accessAuthorization);
        }

        Object o = redisService.get(INIT + id);

        RedisAuthorization initAuthorization = JSON.parseObject(JSON.toJSONString(o), RedisAuthorization.class);

        if (initAuthorization != null){
            return toObject(initAuthorization);
        }

        return null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String key = "";
        if (tokenType == null){
            List<Object> strings = redisService.mGet(CODE + token, TOKEN + DigestUtils.sha256Hex(token), REFRESH + token);
            for (Object one : strings) {
                String string = (String) one;
                if (StringUtils.hasText(string)){
                    return findById(string);
                }
            }
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            key = STATE+token;
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            key = CODE+token;
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            key = TOKEN+DigestUtils.sha256Hex(token);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            key = REFRESH+token;
        }

        Object one = redisService.get(key);
        if (one == null){
            return null;
        }
        return findById((String) one);
    }


    private OAuth2Authorization toObject(RedisAuthorization entity) {
        RegisteredClient registeredClient = this.registeredClientRepository.findById(entity.getRegisteredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '" + entity.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(entity.getId())
                .principalName(entity.getPrincipalName())
                .authorizationGrantType(resolveAuthorizationGrantType(entity.getAuthorizationGrantType()))
                .authorizedScopes(StringUtils.commaDelimitedListToSet(entity.getAuthorizedScopes()))
                .attributes(attributes -> attributes.putAll(parseMap(entity.getAttributes())));
        if (entity.getState() != null) {
            builder.attribute(OAuth2ParameterNames.STATE, entity.getState());
        }

        if (entity.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    entity.getAuthorizationCodeValue(),
                    entity.getAuthorizationCodeIssuedAt(),
                    entity.getAuthorizationCodeExpiresAt());
            builder.token(authorizationCode, metadata -> metadata.putAll(parseMap(entity.getAuthorizationCodeMetadata())));
        }

        if (entity.getAccessTokenValue() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    entity.getAccessTokenValue(),
                    entity.getAccessTokenIssuedAt(),
                    entity.getAccessTokenExpiresAt(),
                    StringUtils.commaDelimitedListToSet(entity.getAccessTokenScopes()));
            builder.token(accessToken, metadata -> metadata.putAll(parseMap(entity.getAccessTokenMetadata())));
        }

        if (entity.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    entity.getRefreshTokenValue(),
                    entity.getRefreshTokenIssuedAt(),
                    entity.getRefreshTokenExpiresAt());
            builder.token(refreshToken, metadata -> metadata.putAll(parseMap(entity.getRefreshTokenMetadata())));
        }

        if (entity.getOidcIdTokenValue() != null) {
            OidcIdToken idToken = new OidcIdToken(
                    entity.getOidcIdTokenValue(),
                    entity.getOidcIdTokenIssuedAt(),
                    entity.getOidcIdTokenExpiresAt(),
                    parseMap(entity.getOidcIdTokenClaims()));
            builder.token(idToken, metadata -> metadata.putAll(parseMap(entity.getOidcIdTokenMetadata())));
        }

        return builder.build();
    }


    private RedisAuthorization toEntity(OAuth2Authorization authorization) {
        RedisAuthorization entity = new RedisAuthorization();
        entity.setId(authorization.getId());
        entity.setRegisteredClientId(authorization.getRegisteredClientId());
        entity.setPrincipalName(authorization.getPrincipalName());
        entity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
        entity.setAuthorizedScopes(StringUtils.collectionToDelimitedString(authorization.getAuthorizedScopes(), ","));
        entity.setAttributes(writeMap(authorization.getAttributes()));
        entity.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        setTokenValues(
                authorizationCode,
                entity::setAuthorizationCodeValue,
                entity::setAuthorizationCodeIssuedAt,
                entity::setAuthorizationCodeExpiresAt,
                entity::setAuthorizationCodeMetadata
        );

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                authorization.getToken(OAuth2AccessToken.class);
        setTokenValues(
                accessToken,
                entity::setAccessTokenValue,
                entity::setAccessTokenIssuedAt,
                entity::setAccessTokenExpiresAt,
                entity::setAccessTokenMetadata
        );
        if (accessToken != null && accessToken.getToken().getScopes() != null) {
            entity.setAccessTokenScopes(StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);
        setTokenValues(
                refreshToken,
                entity::setRefreshTokenValue,
                entity::setRefreshTokenIssuedAt,
                entity::setRefreshTokenExpiresAt,
                entity::setRefreshTokenMetadata
        );

        OAuth2Authorization.Token<OidcIdToken> oidcIdToken =
                authorization.getToken(OidcIdToken.class);
        setTokenValues(
                oidcIdToken,
                entity::setOidcIdTokenValue,
                entity::setOidcIdTokenIssuedAt,
                entity::setOidcIdTokenExpiresAt,
                entity::setOidcIdTokenMetadata
        );
        if (oidcIdToken != null) {
            entity.setOidcIdTokenClaims(writeMap(oidcIdToken.getClaims()));
        }


        return entity;
    }


    private void setTokenValues(
            OAuth2Authorization.Token<?> token,
            Consumer<String> tokenValueConsumer,
            Consumer<Instant> issuedAtConsumer,
            Consumer<Instant> expiresAtConsumer,
            Consumer<String> metadataConsumer) {
        if (token != null) {
            OAuth2Token oAuth2Token = token.getToken();
            tokenValueConsumer.accept(oAuth2Token.getTokenValue());
            issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
            expiresAtConsumer.accept(oAuth2Token.getExpiresAt());
            metadataConsumer.accept(writeMap(token.getMetadata()));
        }
    }


    /**
     * 处理授权申请时的 GrantType
     *
     * @param authorizationGrantType 授权申请时的 GrantType
     * @return AuthorizationGrantType的实例
     */
    private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        // Custom authorization grant type
        return new AuthorizationGrantType(authorizationGrantType);
    }


    private Map<String, Object> parseMap(String data) {
        try {
            return MAPPER.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
    /**
     * 将map对象转为json字符串
     *
     * @param metadata map对象
     * @return json字符串
     */
    private String writeMap(Map<String, Object> metadata) {
        try {
            return MAPPER.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }


    public Long getExpireToken(OAuth2Authorization.Token token){
        if (token!=null){
            Instant expiresAt = token.getToken().getExpiresAt();
            if (expiresAt == null){
                return 1L;
            }
            long aLong = expiresAt.getEpochSecond();
            return aLong - System.currentTimeMillis()/1000;
        }
        return 1L;
    }

}
