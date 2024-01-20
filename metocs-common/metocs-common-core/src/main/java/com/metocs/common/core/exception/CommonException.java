package com.metocs.common.core.exception;

public class CommonException extends RuntimeException{

    private Integer code;

    public CommonException() {
        super();
    }

    public CommonException(String message) {
        super(message);
    }


    public CommonException(Integer code,String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
