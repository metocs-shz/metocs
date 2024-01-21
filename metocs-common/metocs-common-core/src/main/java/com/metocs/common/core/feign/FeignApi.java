package com.metocs.common.core.feign;

import java.lang.annotation.*;

/**
 * @author metocs
 * @date 2024/1/21 13:50
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignApi {
}
