package com.bkjk.housing.contractaward.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FieldMappingVo {
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("编辑器code")
    private String editorCode;

    @ApiModelProperty("编辑器name")
    private String editorName;

    @ApiModelProperty("进件code")
    private String loanCode;

    @ApiModelProperty("进件name")
    private String loanName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("页码")
    private Integer pageNo;

    @ApiModelProperty("每页数量")
    private Integer pageSize;
}
