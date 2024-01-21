package com.metocs.common.core.response;

public enum ResponseEnum {

    OK(200, "操作成功！"),

    FAIL(500, "服务器内部错误！"),

    HTTP_MESSAGE_NOT_READABLE(400, "请求参数格式有误！"),

    HTTP_METHOD_NOT_SUPPORTS(405, "请求方式错误！"),

    ACCESS_DENIED(403,"未授权访问！" ),

    NOT_ACCESS(401,"登录认证已过期！" ),

    ;

    private final Integer code;

    private final String message;

    public Integer code() {
        return code;
    }

    public String message() {
        return message;
    }

    ResponseEnum(Integer code, String msg) {
        this.code = code;
        this.message = msg;
    }

    @Override
    public String toString() {
        return "ResponseEnum{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
