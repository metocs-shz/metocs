package com.metocs.common.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author metocs
 * @date 2024/1/30 22:54
 */
public class Assert {

    private final static Logger logger = LoggerFactory.getLogger(Assert.class);

    public static void hasText(@Nullable String text, String message) {
        if (!StringUtils.hasText(text)) {
            logger.error("参数值为空！ 信息： {}",message);
            throw new CommonException(message);
        }
    }


}
