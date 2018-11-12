package com.bkjk.housing.contractaward.api.vo;

import com.bkjk.housing.common.constant.JsonConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class JsonDto implements Serializable {
    private static final long serialVersionUID = 6065214797170354843L;

    private Integer code;

    private String msg;

    private Map<String, Object> data = new HashMap<>();

    public JsonDto() {
        this.code = JsonConstants.OK;
        this.msg = JsonConstants.OK_INFO;
    }

    public JsonDto(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
