package com.bkjk.housing.common.thirdcenter.capitalplatform.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FundInfoRequest {
    private String fundLoanNo;     //借款单号
    private String productLine;       //产品线 租赁消费金融：L_CONSUM_FINANCE 装修金融：L_DECORATION_FINANCE 房产金融：L_HOUSE_FINANCE
    private String productId;      //产品ID
    private String productName;      //产品名称
    private String financeOrderNo; //业务单号
    private String mortgageType;   //担保方式-new 信用、保证、抵押、质押
    private String loanCity;       //进件城市-new
    private String loanPurpose;    //借款用途-new
    private String loanContractNo; //借款合同编号
    private String certValidity;   // 业务提供-借款人证件有效期
    private String notifyUrl;       //异步通知地址

    private String repayMode;      //还款方式 1 ：等额本息2 ：等额本金3 ：利随本清9 ：其他方式
    private String loanApplyTime;  //借款申请时间
    private BigDecimal loanAmount;     //借款金额
    private int loanTerm;       //借款期限
    private String termUnit;       //期限单位
    private BigDecimal signRate;    // 期望利率
    private String borrowerType;   //借款人类型 1个人、2企业
    private String needMortContract; //是否需要抵押合同 1是
    private String borrowerNum;   //借款人个数
    private String allowPrepayment; //是否允许提前还款-new 1是 0否
    // 放款信息
    private LoanInfo loanInfo;
    // 借款人信息
    private List<BorrowerInfo> borrowerInfoList;
    // 企业借款人信息
    private List<LoanOrgInfo> loanOrgInfoList;
    // 借款人婚姻情况
    private List<BorrowerInfo> spouseInfoList;
    // 关联人信息
    private List<RelaPersonVo> relaPersonList;
    // 抵押相关信息
    private List<HouseMortgageInfoVo> mortgageInfo;
    // 抵押品共有人
    private List<HouseMortgageJointVo> mortgageJointVoList;
    // 权证
    private List<CollInfo> collList;
    // 赎楼贷款信息
    private List<RedemptionBuild> redemptionBuildList;

    /**
     * 放款信息
     */
    @Data
    public static class LoanInfo {
        private String accountType;    //账号类型  对公、对私
        private String accountNo;      //放款账号
        private String bankName;       //放款账号银行
        private String bankAddress;    //放款账号支行名称
        private String bankUnionNo;    //放款账号联行号
        private String bankCode;       //放款账号银行编号
        private String accountName;    //放款账户名
        private String bankCardType;   //放款账号开户证件类型
        private String bankCardNo;     //放款账号开户证件号
    }

    /**
     * 个人借款人信息
     */
    @Data
    public static class BorrowerInfo {
        private String borrowerName;   //借款人姓名
        private String telePhone;      //借款人手机号
        private String idType;         //证件类型
        private String idCardNo;       //证件号
        private String maritalStatus;  //婚姻状况
        private String spouseName;     //配偶姓名-new
        private String spouseIdTyp;    //配偶证件类型-new
        private String spouseIdNo;     //配偶证件号码-new
        private String spouseMobile;   //配偶联系电话-new
    }

    /**
     * 借款企业信息
     */
    @Data
    public static class LoanOrgInfo {
        private String loanOrgName;    //企业名称-new
        private String orgCertiType;   //企业证件类型-new
        private String orgCertiCode;   //企业证件号码-new
        private String customerName;   //法人姓名-new
        private String legalCertType;  //法人证件类型-new
        private String legalCertNo;    //法人证件号码-new
        private String legalMobileNo;  //法人手机号码-new
    }

    /**
     * 关联人信息
     */
    @Data
    public static class RelaPersonVo {
        private String relaPerType;    //关联人类型-new
        private String relaPerName;    //关联人姓名-new
        private String relaPerSex;     //关联人性别-new
        private String relaPerMoblie;  //关联人手机号-new
        private String relaPerCertNo;  //关联人证件号-new
        private String relaPerCertType; //关联人证件类型-new
    }

    /**
     * 抵押相关信息
     */
    @Data
    public static class HouseMortgageInfoVo {
        private String assureType;        //担保类型00抵押、01质押
        private String mortgagesType;   //押品类型 0房产
        private String ownerName;       //权属人姓名
        private String ownerIDType;     //权属人证件类型
        private String ownerID;         //权属人证件号码-new
        private String buildingType;    //00住宅、01商品房、02央产房、03经济适用房、04按经济适用房管理、05房改房-成本价、06房改房-标准价、07限价房、08回迁房、09军产房、10校产房、11其它、13公寓、14经转商、15未支付综合地价款（土地出让金）经济适用房、16已购公房、17商品房-住宅、18商品房-公寓、19商品房-商住两用、20商品房-商铺、21商品房-其他
        private String buildingProvince; //房产地址（省）
        private String buildingCity;    //房产地址（市）
        private String buildingArea;    //房产地址（区）
        private String buildingAddr;    //房产详细地址
        private BigDecimal buildSquare; //建筑面积
        private BigDecimal squarePrice; //面积单价
    }

    /**
     * 抵押品共有人
     */
    @Data
    public static class HouseMortgageJointVo {
        private String jointName;    //共有人姓名-new
        private String jointIDType;  //共有人证件类型-new
        private String jointID;    //共有人证件号码-new
    }

    /**
     * 权证基本信息
     */
    @Data
    public static class CollInfo {
        private String collIDNo; //	权利凭证号
        private String collType; //	权证类型
        private String collStartDate; //	权证发证日期
        //	private String collEndDate;//	权证到期日期
        private String collOrgName; //	权证发证机关名称
    }

    @Data
    public static class RedemptionBuild {
        private BigDecimal tradeAmt;        //二手房交易金额-new
        private String mortgagePerson;        //抵押权人-new
        private BigDecimal mortgageAmt;        //抵押金额-new
    }
}
