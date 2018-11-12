package com.bkjk.housing.contractaward.passport.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginUserBo {
    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "账户")
    private String account;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "电话")
    private String phone;
    @ApiModelProperty(value = "头像")
    private String avatar;
    @ApiModelProperty(value = "上级领导code")
    private Long superiorCode;
    @ApiModelProperty(value = "上级领导名")
    private String superiorName;
    @ApiModelProperty(value = "组织code")
    private String orgCode;
    @ApiModelProperty(value = "组织名")
    private String orgName;
    @ApiModelProperty(value = "职位code")
    private String positionCode;
    @ApiModelProperty(value = "职位名称")
    private String positionName;
    @ApiModelProperty(value = "职级")
    private Integer positionLevel;
    @ApiModelProperty(value = "城市")
    private String cityCode;
}
