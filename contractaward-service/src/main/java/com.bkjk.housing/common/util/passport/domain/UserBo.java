package com.bkjk.housing.common.util.passport.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserBo {
    @ApiModelProperty(value = "用户id")
    private String userCode;
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
    private String superiorCode;
    @ApiModelProperty(value = "上级领导名")
    private String superiorName;
    @ApiModelProperty(value = "组织code")
    private String orgCode;
    @ApiModelProperty(value = "组织名")
    private String orgName;
    @ApiModelProperty(value = "职位code")
    private String positionCode;
    @ApiModelProperty(value = "角色集合")
    private List<RoleBo> roles;
    @ApiModelProperty(value = "角色集合")
    private String roleNames;
}
