package com.metocs.web.feign;

import com.metocs.common.core.constant.BaseConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * Feign 客户端统一配置
 */
@Configuration
@EnableFeignClients(basePackages = "com.metocs")
public class FeignConfiguration implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            HttpServletRequest request = attributes.getRequest();

            //设置Feign调用为内部请求
            requestTemplate.header(BaseConstant.X_INNER_API,"true");

            //数据源切换的依据
            String tenant = request.getHeader(BaseConstant.TENANT);
            requestTemplate.header(BaseConstant.TENANT, tenant);

            //请求的Id
            String trance = request.getHeader(BaseConstant.TRACE_HEADER);
            requestTemplate.header(BaseConstant.TRACE_HEADER, trance);

            //请求的Token
            if (StringUtils.hasText(BaseConstant.AUTHORIZATION)){
                String authorization = request.getHeader(BaseConstant.AUTHORIZATION);
                requestTemplate.header(BaseConstant.AUTHORIZATION, authorization);
            }
        }
    }
}
