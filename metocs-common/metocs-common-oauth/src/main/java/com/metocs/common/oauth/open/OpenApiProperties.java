package com.metocs.common.oauth.open;

import com.metocs.common.core.feign.FeignApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author metocs
 * @date 2024/1/21 13:44
 */
@Component
@ConfigurationProperties(prefix = "open")
public class OpenApiProperties {

    private Set<String> apis = new HashSet<>();

    public Set<String> getApis() {
        return apis;
    }

    public void setApis(Set<String> apis) {
        this.apis = apis;
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * add openApi and FeignApi
     * @return OPEN APIS
     */
    public Set<String> apis(){
        RequestMappingHandlerMapping bean = webApplicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();
            OpenAPI openAPI = method.getMethodAnnotation(OpenAPI.class);

            FeignApi feignApi = method.getMethodAnnotation(FeignApi.class);
            if (openAPI==null && feignApi == null){
                continue;
            }
            Set<String> patternValues = info.getPatternValues();
            apis.addAll(patternValues);
        }
        return this.apis;
    }

}
