package com.bkjk.housing.common.enums;

public enum ContractCompleteStatusEnum {

    SAVE(1, "保存"),
    COMMIT(2, "提交");

    private Integer code;
    private String description;

    ContractCompleteStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
