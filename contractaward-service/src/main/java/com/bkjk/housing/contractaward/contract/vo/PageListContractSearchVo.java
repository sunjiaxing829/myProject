package com.bkjk.housing.contractaward.contract.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PageListContractSearchVo extends BasePageVo {

    private Integer contractId;

    //金融主协议编号
    private String contractNo;

    //产品名称
    private String productName;

    //产品编码
    private String productNo;

    //合同名称
    private String contractName;

    //客户名称
    private String customerName;

    //借款金额
    private BigDecimal customerLoanAmount;

    //借款期限
    private Integer loanTerm;

    //折扣
    private BigDecimal discount;

    //折扣start
    private BigDecimal startDiscount;

    //折扣end
    private BigDecimal endDiscount;

    //主协议状态
    private String contractStatus;

    //补充协议数量
    private long supplementalCount;

    //解约协议数量
    private long terminationCount;

    //搜索人员信息使用
    private String userCode;

    //顾问名
    private String userName;

    //资金类借款金额
    private BigDecimal borrowingAmount;

    //资金类借款折扣
    private BigDecimal fundDisCount;

    //资金类借款期限
    private Integer borrowingTime;

    //产品类型
    private Integer productType;

    //录入类型
    private Byte inputType;

    //城市名称
    private String cityName;
}
