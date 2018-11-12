package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ContractImageVo {


    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("协议编号")
    private String agreementNo;

    @ApiModelProperty("图片地址")
    private List<String> imageUrlList;

    @ApiModelProperty("备注")
    private String remark;
}
