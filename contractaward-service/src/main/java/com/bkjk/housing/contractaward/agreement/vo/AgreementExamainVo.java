package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AgreementExamainVo {
    @ApiModelProperty(value = "协议号", required = true)
    private String agreementNo;
    @ApiModelProperty(value = "备注", required = false)
    private String remark;
}
