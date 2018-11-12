package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum SupplementAgreementEnum {


    /**
     * 补充合同状态
     */
    SUPPLEMENT_START(0, "发起合同补充"),
    SUPPLEMENT_SUCCESS(1, "补充成功"),
    SUPPLEMENT_FAILURE(2, "补充失败");

    private Integer code;
    private String name;
}
