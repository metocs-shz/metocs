package com.metocs.common.core.feign;

import com.metocs.common.core.constant.BaseConstant;
import com.metocs.common.core.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author metocs
 * @date 2024/1/21 13:50
 */
@Aspect
@Component
public class FeignApiAspect {

    private final static Logger logger = LoggerFactory.getLogger(FeignApiAspect.class);


    @Autowired
    private HttpServletRequest httpServletRequest;

    @Before("@annotation(feignApi)")
    public void before(FeignApi feignApi){

        String header = httpServletRequest.getHeader(BaseConstant.X_INNER_API);

        String traceId = httpServletRequest.getHeader(BaseConstant.TRACE_HEADER);

        logger.debug("请求：{} 进入内部接口访问验证：{}",traceId,header);

        if ("true".equals(header)){
            return;
        }

        throw new CommonException(403,"内部API禁止访问！");
    }


}
