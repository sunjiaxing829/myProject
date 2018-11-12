package com.bkjk.housing.contractaward.contract.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CaculateVo {

    @ApiModelProperty(value = "产品编号")
    private String productNo;

    @ApiModelProperty(value = "借款金额、担保金额")
    private BigDecimal useAmount;

    @ApiModelProperty(value = "房屋抵押状况")
    private Integer houseMortgage;
}
