package com.bkjk.housing.common.util.passport.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleBo {

    @ApiModelProperty(value = "城市code")
    private String cityCode;

    @ApiModelProperty(value = "城市名")
    private String cityName;

    @ApiModelProperty(value = "职位类型")
    private String positionCode;

    @ApiModelProperty(value = "角色类型")
    private Integer type;

    @ApiModelProperty(value = "角色名")
    private String roleName;

    @ApiModelProperty(value = "角色id")
    private String roleId;
}
