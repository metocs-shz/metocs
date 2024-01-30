package com.metocs.common.core.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class ResponseData<T> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ResponseData.class);

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void detail(ResponseEnum responseEnum){
        this.message = responseEnum.message();
        this.code = responseEnum.code();
    }

    public static <T> ResponseData<T> success() {
        ResponseData<T> result = new ResponseData<>();
        result.detail(ResponseEnum.OK);
        return result;
    }

    public static <T> ResponseData<T> success(T data) {
        ResponseData<T> result = new ResponseData<>();
        result.setData(data);
        result.detail(ResponseEnum.OK);
        return result;
    }

    public static <T> ResponseData<T> fail(ResponseEnum responseEnum) {
        logger.error("请求出现异常 {}",responseEnum.toString());
        ResponseData<T> result = new ResponseData<>();
        result.detail(responseEnum);
        return result;
    }

    public static <T> ResponseData<T> fail(String message) {
        logger.error("请求出现异常 {}", message);
        ResponseData<T> result = new ResponseData<>();
        result.detail(ResponseEnum.FAIL);
        result.setMessage(message);
        return result;
    }

    public static <T> ResponseData<T> fail(ResponseEnum responseEnum,String message) {
        logger.error("请求出现异常 {}", message);
        ResponseData<T> result = new ResponseData<>();
        result.detail(responseEnum);
        result.setMessage(message);
        return result;
    }

    public static <T> ResponseData<T> fail(ResponseEnum responseEnum,String message,T data) {
        logger.error("请求出现异常 {}", message);
        ResponseData<T> result = new ResponseData<>();
        result.detail(responseEnum);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
