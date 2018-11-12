package com.bkjk.housing.contractaward.contract.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BasePageVo {
    @ApiModelProperty(value = "当前页数")
    private int pageSize;
    @ApiModelProperty(value = "每页大小")
    private int pageNum;
}
