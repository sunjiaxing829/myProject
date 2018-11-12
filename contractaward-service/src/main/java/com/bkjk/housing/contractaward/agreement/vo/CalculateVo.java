package com.bkjk.housing.contractaward.agreement.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculateVo {
    private BigDecimal useAmount;
    private String productNo;
    private String loanNo;
    private BigDecimal contractFee;
    private Integer houseMortgage;
}
