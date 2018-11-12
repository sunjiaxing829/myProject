package com.bkjk.housing.common.thirdcenter.capitalplatform.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FundInfoResponseBo implements Serializable {

    /**
     * 响应码:S0000 成功
     * S0001 接受成功
     * E1001 失败
     */
    private String returnCode;

    /**
     * 响应信息
     */
    private String returnMsg;

    /**
     * 业务单号
     */
    private String financeOrderNo;

    /**
     * 借款单号
     */
    private String fundLoanNo;

    /**
     * 选定资金方
     */
    private String checkedChannelCode;
}
