package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum CommonTypeEnum {

    /**
     * 类型
     */
    ZERO(0, "0"),
    ONE(1, "1"),
    TWO(2, "2");

    private Integer status;
    private String description;
}
