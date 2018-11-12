package com.bkjk.housing.common.thirdcenter.finance.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateBo {
    private Integer mortgageflag;
    private String queryId;
    private String productId;
    private Integer productLine;
    private Integer productType;
    private Integer repayMethod;
    private BigDecimal loanAmt;
    private Integer loanDay;
    private Integer defineRepay;
    private List<ChargeBo> repayPlans;
}
