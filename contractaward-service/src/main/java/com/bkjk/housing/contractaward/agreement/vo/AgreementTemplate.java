package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AgreementTemplate {
    @ApiModelProperty("模板版本id")
    private Long templateVersionId;
    @ApiModelProperty("应用编码")
    private String applicationCode;
    @ApiModelProperty("租户编码")
    private String tenantCode;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("编码")
    private String code;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("内容md5")
    private String contentMd5;
    @ApiModelProperty("预览地址")
    private String url;
    @ApiModelProperty("盖章预览地址")
    private String sealUrl;
}
