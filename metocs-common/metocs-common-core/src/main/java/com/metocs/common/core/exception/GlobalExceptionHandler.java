package com.metocs.common.core.exception;


import com.metocs.common.core.response.ResponseData;
import com.metocs.common.core.response.ResponseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseData<Object> MethodNotSupportedException(MissingServletRequestParameterException e) {
        this.logger.error("请求参数错误: {}", e.getMessage(),e);
        return ResponseData.fail(ResponseEnum.HTTP_MESSAGE_NOT_READABLE);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseData<Object> MethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        this.logger.error("请求方式错误: {}", e.getMessage(),e);
        return ResponseData.fail(ResponseEnum.HTTP_METHOD_NOT_SUPPORTS);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseData<Object> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String message = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";"));
        this.logger.error("请求参数错误: {}", e.getMessage(),e);
        return ResponseData.fail(ResponseEnum.HTTP_MESSAGE_NOT_READABLE,message);
    }

    @ExceptionHandler(value = CommonException.class)
    public ResponseData<Object> exceptionHandler(CommonException e) {
        this.logger.error("出现自定义错误: {}", e.getMessage(),e);
        return ResponseData.fail(e.getMessage());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseData<Object> exceptionHandler(RuntimeException e) {
        this.logger.error("服务出现异常: {}", e.getMessage(),e);
        return ResponseData.fail(ResponseEnum.FAIL);
    }

}
