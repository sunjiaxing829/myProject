package com.bkjk.housing.contractaward.contract.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ContractCityVo {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("城市Code")
    private String cityCode;

    @ApiModelProperty("城市名称")
    private String cityName;

    @ApiModelProperty("城市编码")
    private String cityCodeNo;
}
