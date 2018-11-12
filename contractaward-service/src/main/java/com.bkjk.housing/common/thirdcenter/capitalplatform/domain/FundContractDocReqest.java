package com.bkjk.housing.common.thirdcenter.capitalplatform.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FundContractDocReqest implements Serializable {

    private String fundLoanNo; //借款单号
    private String productLine; //产品线
    private String productId; //产品ID
    private String productName; //产品名称
    private String financeOrderNo; //业务单号
    private String creatContType; //生成合同类型 loan-贷款合同，dy-抵押合同
    private String prdContNo;         //合同编号
    private BigDecimal loanAmt;           //贷款金额
    private String loanTermUnit;      //借款期限单位
    private int loanTerm;            //借款期限
    private BigDecimal contractRate;  //正常执行利率
    private String receAccName;       //放款账户名
    private String receAccNo;         //放款账号
    private String receAccBankName;       //放款账号开户行名
    private String receAccBchName;    //放款账号支行名称

    private PrdContInfo prdContInfo; //贷款合同 (类型为贷款必填)
    private List<ApplyCustInfo> applyCustInfoList; //借款人基本信息(类型为贷款必填)
    private DYContInfo dyContInfo; //抵押合同(类型为抵押必填)
    private List<Mortgagor> mortgagorList; //抵押人基本信息(类型为抵押必填)

    private String notifyUrl; //签约回掉地址

    @Data
    public static class PrdContInfo {
        private String prdContVerNum;     //合同版本号
        private String prdContType;       //合同类型
        private String contProvince;      //省
        private String contCity;          //城市
        private String contArea;          //区县
        private String dyContSignDate;    //合同生成日期
        private String loanPurpose;       //借款用途
        private String loanPurposeOther;  //用途备注
        private int contFileNum;          //合同份数
    }

    @Data
    public static class ApplyCustInfo {

        private String appCustName;      //借款人姓名
        //借款人证件类型20-身份证、22-护照、23-军官证、25-港澳居民来往内地通行证、26-台湾居民来往大陆通行证、2X-其他证件、30-组织机构代码证、31-营业执照
        private String appCertType;
        private String appCertNo;        //证件号码
        private String appCustMobile;    //联系电话
        private String appCustAddr;      //联系地址
        private String repLegalCustName; //法定代表人
    }

    @Data
    public static class DYContInfo {
        private String dyContVerNum;   //合同版本号
        private String mortContractNo; //抵押合同编号
        private String dyContSignDate; //合同生成日期
        private String dyContProvince; //省
        private String dyContCity;     //城市
        private String dyContArea;     //区县
        private BigDecimal dyLoanAmt;      //借款金额
        private String buildingNo;     //抵押物权属证书编号
        private String buildingAddr;   //抵押物坐落地址
        private int dyContFileNum;     //合同份数
        private int mortgagorNum;      //抵押人个数
    }

    @Data
    public static class Mortgagor {
        private String mortgagorName;        //抵押人姓名
        private String mortgagorCertType;   //抵押人证件类型
        private String mortgagorCertNo;     //证件号码
        private String mortgagorMobile;     //联系电话
        private String mortgagorAddr;       //联系地址
        private String dyRepLegalCustName;  //法定代表人
    }
}
