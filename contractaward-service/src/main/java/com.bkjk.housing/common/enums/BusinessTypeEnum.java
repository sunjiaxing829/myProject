package com.bkjk.housing.common.enums;

public enum BusinessTypeEnum {

    /**
     * 协议类型
     */
    MAIN("MAIN", "主协议"),
    REPLENISH("REPLENISH", "补充协议"),
    RECISSION("RECISSION", "解约协议");

    private String code;
    private String description;

    BusinessTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByCode(String code) {
        for (BusinessTypeEnum businessTypeEnum : BusinessTypeEnum.values()) {
            if (businessTypeEnum.getCode().equalsIgnoreCase(code)) {
                return businessTypeEnum.getDescription();
            }
        }
        return null;
    }

    public static BusinessTypeEnum getBusinessTypeEnum(String code) {
        for (BusinessTypeEnum businessTypeEnum : BusinessTypeEnum.values()) {
            if (businessTypeEnum.getCode().equalsIgnoreCase(code)) {
                return businessTypeEnum;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
