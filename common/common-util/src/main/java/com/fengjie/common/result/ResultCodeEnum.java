package com.fengjie.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(2012, "服务异常"),
    DATA_ERROR(204, "数据异常"),

    LOGIN_AUTH(203, "未登陆"),
    PERMISSION(209, "没有权限"),
    LOGIN_MOBLE_ERROR(208,"认证失败" );
    private Integer code;
    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
