package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

//解约类型：0： 发起解约 1：解约成功 2：解约失败
@AllArgsConstructor
@Getter
@ToString
public enum TerminationAgreementEnum {

    /**
     * 解约结构
     */
    TERMINATION_START(0, "发起解约"),
    TERMINATION_SUCCESS(1, "解约成功"),
    TERMINATION_FAILURE(2, "解约失败");

    private Integer code;
    private String name;
}
