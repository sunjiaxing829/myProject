package com.bkjk.housing.common.enums;

import lombok.Getter;

/**
 * 金融单状态
 */
@Getter
public enum LoanStatusEnum {

    NULL(0, "未知"),
    NOT_REPORT(11, "未报"),
    REPORT_AGAIN(12, "重新报单"),
    RISK_CONTROL_AUDIT_ING(21, "风控审核中"),
    RISK_CONTROL_AUDIT_SUCCESS(22, "已出告知书"),
    CANCEL_CONTRACT_ING(23, "解约中"),
    CONTRACT_CHANGE(24, "合同变更"),
    REMIT_MONEY(25, "已划款"),
    TRANSFER_CONFIRMATION(26, "已过户"),
    CONTRACT_SUPPLE(27, "合同补充"),
    RISK_AUDIT_SUCCESS(28, "风控审核通过"),
    ALREADY_LEND(29, "已放款"),
    PARTIAL_BACK(210, "部分回款"),
    ALL_BACK(211, "全部回款"),
    CANCELLED(212, "已解约"),
    NOT_REACHED(213, "未达成"),
    LOAN_FAILURE(214, "放款失败"),
    REMIT_MONEY_READY(215, "待划款"),
    REMIT_MONEY_FAILURE(216, "划款失败"),
    ABORTED_CANCEL_CONTRACT(31, "已中止（解约）"),
    ABORTED_INVALID(32, "已中止（无效）"),
    END_NOT_REACHED(33, "已终结（未达成）"),
    END(34, "已终结"),
    END_LOAN_FAILURE(35, "已中止（放款失败）");

    private Integer code;

    private String status;

    LoanStatusEnum(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public static LoanStatusEnum getOrderStatus(Integer code) {
        for (LoanStatusEnum status : LoanStatusEnum.values()) {
            if (code == status.getCode()) {
                return status;
            }
        }
        return LoanStatusEnum.NULL;
    }
}
