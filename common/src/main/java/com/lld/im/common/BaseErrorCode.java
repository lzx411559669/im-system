package com.lld.im.common;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.common
 * @data 2023/7/3 23:11
 */
public enum BaseErrorCode implements ApplicationExceptionEnum {
    SUCCESS(200,"success"),
    SYSTEM_ERROR(90000,"服务器内部错误,请联系管理员"),
    PARAMETER_ERROR(90001,"参数校验错误");

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getError() {
        return null;
    }

    private int code;

    private String error;

    BaseErrorCode(int code,String error) {
        this.code = code;
        this.error = error;
    }
}
