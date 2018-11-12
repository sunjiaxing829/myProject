package com.bkjk.housing.contractaward.api.vo;

import lombok.Data;

@Data
public class CapitalPlatformVo {
    private String financeOrderNo; // 业务单号
    private String fundLoanNo; // 借款单号
    private String fileName;
    private String contractNo;
    private String url;
}
