package com.bkjk.housing.contractaward.agreement.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AgreementSaveVo {

    @ApiModelProperty("主键id")
    private Long id;
    @ApiModelProperty("合同id")
    private Long contractId;
    @ApiModelProperty("进件编号")
    private String loanNo;
    @ApiModelProperty("模版id")
    private Long templateId;
    @ApiModelProperty("业务类型，1:房款支付担保类，2:借款担保类，3:委托居间类，4:消费金融借款类，5:其他")
    private String templateType;
    @ApiModelProperty("协议类型（1:主协议，2:补充协议，3:解约协议）")
    private String businessType;
    @ApiModelProperty("协议编号")
    private String agreementNo;
    @ApiModelProperty("协议名称（模板名称）")
    private String agreementName;
    @ApiModelProperty("合同填写状态:1-保存;2-提交")
    private Integer contractCompleteStatus;
    @ApiModelProperty("创建人")
    private Long createdId;
    @ApiModelProperty("创建人名")
    private String createdName;
    @ApiModelProperty("合同详情")
    private Map<String, Object> detailJsonObject;
}
