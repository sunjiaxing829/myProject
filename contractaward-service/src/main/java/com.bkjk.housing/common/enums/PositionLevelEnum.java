package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 职级枚举
 */
@AllArgsConstructor
@Getter
@ToString
public enum PositionLevelEnum {

    /**
     * 职级
     */
    ATTACHE(0, "专员"),
    CHARGE(1, "主管"),
    MANAGER(2, "经理"),
    ASSISTANTMANAGER(3, "副总经理"),
    GENERALMANAGER(4, "总经理"),
    MAJORDOMO(5, "总监"),
    SENIORMANAGER(6, "高级经理"),
    VICEPRESIDENT(7, "副总裁"),
    PRESIDENT(8, "总裁"),
    SENIORDIRECTOR(9, "高级总监");


    private Integer levelCode;
    private String levelName;
}
