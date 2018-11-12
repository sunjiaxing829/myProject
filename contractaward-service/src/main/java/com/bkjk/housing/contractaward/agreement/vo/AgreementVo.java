package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AgreementVo {
    @ApiModelProperty(value = "协议编号")
    private String agreementNo;
    @ApiModelProperty(value = "协议名称")
    private String agreementName;
    @ApiModelProperty(value = "协议类型")
    private String businessType;
    @ApiModelProperty(value = "借款金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "产品编号")
    private String productNo;
    @ApiModelProperty(value = "顾问姓名")
    private String advisorName;
    @ApiModelProperty(value = "提交时间开始")
    private String createTimeStart;
    @ApiModelProperty(value = "提交时间结束")
    private String createTimeEnd;
    @ApiModelProperty(value = "时间搜索顺序")
    private  String searchTimeFlag;
    @ApiModelProperty(value = "合同状态集合")
    private List<String> statusList;
    @ApiModelProperty(value = "搜索关键字")
    private  String searchKey;
    @ApiModelProperty(value = "查询页")
    private Integer pageNo;
    @ApiModelProperty(value = "每页格式")
    private Integer pageSize;
}
