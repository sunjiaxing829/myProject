package com.bkjk.housing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum ContractStatusEnum {
    /**
     * 签约状态
     */
    DRAFT(1, "草拟中"),
    INVALIDED(2, "已无效"),
    AUDITING(3, "审核中"),
    APPROVE(4, "审核通过"),
    SEALED(5, "已盖章"),
    SIGNED(6, "已签约"),
    SUPPLEMENTING(7, "补充中"),
    TERMINATING(8, "解约中"),
    CHANGING(9, "变更中"),
    TERMINATED(10, "已解约"),
    FINNISH(11, "已完结"),
    ARCHIVE(12, "已归档"),
    REJECTED(13, "已驳回"),
    SEALING(14, "盖章中");

    private Integer status;
    private String description;


}
