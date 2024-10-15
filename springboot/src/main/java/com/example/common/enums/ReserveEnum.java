package com.example.common.enums;

public enum ReserveEnum {

    DONE("已做"),
    NO("不"),
    ;

    public String status;

    ReserveEnum(String status) {
        this.status = status;
    }
}
