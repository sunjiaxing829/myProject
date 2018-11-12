package com.bkjk.housing.common.thirdcenter.finance.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChargeBo {
    private Integer chargeId;
    private BigDecimal realAmt;
    private List<Integer> tems;
}
