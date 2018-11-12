package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum ProductTypeEnum {
    /**
     * 产品类型
     */
    FUND("FUND", "资金", 1),
    GUARANTEE("GUARANTEE", "担保", 2),
    INTERMEDIARY("INTERMEDIARY", "居间", 3),
    SERVICE("SERVICE", "服务类",4);

    private String code;
    private String description;
    private Integer index;

    public static String getProductTypeCodeByName(String name) {
        for (ProductTypeEnum productTypeEnum : ProductTypeEnum.values()) {
            if (productTypeEnum.name().equalsIgnoreCase(name)) {
                return productTypeEnum.getCode();
            }
        }
        return null;
    }

    /**
     * 提审推送台账新接口：10居间担保，11居间资金，12服务类
     */
    public static Integer getNewProductTypeIdByCode(String code) {
        if (code.equals(ProductTypeEnum.FUND.getCode())) return 11;
        if (code.equals(ProductTypeEnum.GUARANTEE.getCode())) return 10;
        if (code.equals(ProductTypeEnum.SERVICE.getCode())) return 12;
        return 0;
    }

    public static Integer getProductTypeIndexByName(String name) {
        for (ProductTypeEnum productTypeEnum : ProductTypeEnum.values()) {
            if (productTypeEnum.name().equalsIgnoreCase(name)) {
                return productTypeEnum.getIndex();
            }
        }
        return null;
    }
}
