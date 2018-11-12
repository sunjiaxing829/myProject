package com.bkjk.housing.common.util;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseResult {
    
    public static final BaseResult SUCCESS = new BaseResult(200, "success");
    
    public static final BaseResult FAIL = new BaseResult(-1, "fail");
    
    public static final BaseResult INVALID_PARAM = new BaseResult(1, "parameter is invalid");
    
    public static final BaseResult UNAUTHORIZED = new BaseResult(401, "unauthorized");
    
    @ApiModelProperty("状态码，200成功")
    private int code;
    
    @ApiModelProperty("消息")
    private String msg;

    public BaseResult() {
    }

    public BaseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
}