package com.bkjk.housing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TemplateTypeEnum {

    /**
     * 模版类型
     */
    HOUSE_PAYMENT_GUARANTEE("HOUSE_PAYMENT_GUARANTEE", "房款支付担保类", 1),
    LOAN_GUARANTEE("LOAN_GUARANTEE", "借款担保类", 2),
    DELEGATE_INTERMEDIATION("DELEGATE_INTERMEDIATION", "委托居间类", 3),
    CONSUMER_FINANCE_BORROWING("CONSUMER_FINANCE_BORROWING", "消费金融借款类", 4),
    OTHER("OTHER", "其他", 5),
    ZHXT_REDEMPTION_LOAN("ZHXT_REDEMPTION_LOAN", "中航信托赎楼贷款合同", 6),
    ZHXT_REDEMPTION_MORTGAGE("ZHXT_REDEMPTION_MORTGAGE", "中航信托赎楼抵押合同", 7),
    ZHXT_FINAL_LOAN("ZHXT_FINAL_LOAN", "中航信托尾款贷款合同", 8),
    LOAN("LOAN", "借款合同", 2001),
    WITHHOLDING("WITHHOLDING", "代扣协议", 2002),
    CREDIT_AUTHORIZATION("CREDIT_AUTHORIZATION", "征信授权协议", 2003);

    private String status;
    private String description;
    private Integer code;
}
