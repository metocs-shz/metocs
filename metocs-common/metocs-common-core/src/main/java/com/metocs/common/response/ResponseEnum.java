package com.metocs.common.response;

public enum ResponseEnum {

    OK("200", "操作成功！"),

    FAIL("500", "服务器内部错误！"),

    HTTP_MESSAGE_NOT_READABLE("400", "请求参数格式有误！"),

    HTTP_METHOD_NOT_SUPPORTS("405", "请求方式错误！"),

    ;

    private final String code;

    private final String message;

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    ResponseEnum(String code, String msg) {
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
