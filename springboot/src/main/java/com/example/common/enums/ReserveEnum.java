package com.example.common.enums;

public enum ReserveEnum {

    DONE("已结束"),
    NO("审核不通过"),
    OK("审核通过"),
    ;

    public String status;

    ReserveEnum(String status) {
        this.status = status;
    }
}
