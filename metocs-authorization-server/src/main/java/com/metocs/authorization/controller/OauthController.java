package com.metocs.authorization.controller;

import com.alibaba.fastjson2.JSON;
import com.metocs.authorization.properties.ClientProperties;
import com.metocs.authorization.properties.SecurityOauthProperties;
import com.metocs.authorization.vo.CaptchaVo;
import com.metocs.common.core.constant.BaseConstant;
import com.metocs.common.core.response.ResponseData;
import com.metocs.common.core.response.ResponseEnum;
import com.metocs.common.core.utils.Base64Utils;
import com.metocs.common.core.utils.HttpClient;
import com.metocs.common.oauth.model.AccessModel;
import com.metocs.common.oauth.service.RedisOAuth2AuthorizationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author metocs
 * @date 2024/1/23 22:35
 */
@RestController
public class OauthController {

    private final static Logger logger = LoggerFactory.getLogger(OauthController.class);


    @Autowired
    private SecurityOauthProperties securityOauthProperties;

    @Autowired
    private RedisOAuth2AuthorizationService redisOAuth2AuthorizationService;

    @GetMapping(value = "captcha")
    public ResponseData<CaptchaVo> captcha(){



        return ResponseData.success();
    }


    @GetMapping(value = "code")
    public ResponseData<AccessModel> code(@RequestParam(value = "code") String code, HttpServletResponse response){
        if (logger.isDebugEnabled()){
            logger.debug("进入 code 兑换token 服务 {}",code);
        }
        OAuth2Authorization oAuth2Authorization = redisOAuth2AuthorizationService.findByToken(code, new OAuth2TokenType("code"));
        if (oAuth2Authorization == null){
            return ResponseData.fail("授权码信息验证失败！");
        }
        //验证授权码使用次数
        if (logger.isDebugEnabled()){
            logger.debug("通过 code 完成 客户端id的获取 {}",oAuth2Authorization.getRegisteredClientId());
        }
        ClientProperties clientProperties = securityOauthProperties.getClient(oAuth2Authorization.getRegisteredClientId());

        Map<String,String> map = new HashMap<>();
        map.put("code",code);
        map.put("redirect_uri",clientProperties.getRedirectUri());
        map.put("grant_type","authorization_code");

        String data = HttpClient.doFormDataPost(clientProperties.getUri(),map ,
                Base64Utils.base64Encode(clientProperties.getClientId()+":"+clientProperties.getClientSecret()));

        AccessModel accessModel = JSON.parseObject(data, AccessModel.class);
        setCookieValue(response,"access_token",accessModel.getAccess_token(),86400);
        setCookieValue(response,"refresh_token",accessModel.getRefresh_token(),86400);
        if (!StringUtils.hasText(accessModel.getAccess_token()) || !StringUtils.hasText(accessModel.getRefresh_token())){
            logger.error("鉴权信息获取失败 {}",data);
            return ResponseData.fail(ResponseEnum.HTTP_MESSAGE_NOT_READABLE,"鉴权信息获取失败",accessModel);
        }
        return ResponseData.success(accessModel);
    }

    @PostMapping(value = "logout")
    public ResponseData<AccessModel> logout(HttpServletRequest request){
        String header = request.getHeader(BaseConstant.AUTHORIZATION);
        OAuth2Authorization oAuth2Authorization = redisOAuth2AuthorizationService.findByToken(header, OAuth2TokenType.ACCESS_TOKEN);
        if (oAuth2Authorization == null){
            return ResponseData.success();
        }
        redisOAuth2AuthorizationService.remove(oAuth2Authorization);
        return ResponseData.success();
    }


    public static void setCookieValue(HttpServletResponse response, String cookieName, String cookieValue, Integer maxAge){
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge); // Cookie的存活时间（自定义）
        cookie.setPath("/"); // 默认路径
        response.addCookie(cookie);
    }

}
