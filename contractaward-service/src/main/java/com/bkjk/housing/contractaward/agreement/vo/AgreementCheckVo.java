package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AgreementCheckVo {
    @ApiModelProperty(value = "合同id")
    private Long contractId;
    @ApiModelProperty(value = "协议no")
    private String agreementNo;
    @ApiModelProperty("合同详情")
    private Map<String, Object> detailJsonObject;
}
