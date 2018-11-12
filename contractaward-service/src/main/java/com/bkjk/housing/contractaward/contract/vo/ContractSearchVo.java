package com.bkjk.housing.contractaward.contract.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractSearchVo {
    @ApiModelProperty(value = "主协议名称")
    private String agreementName;
    @ApiModelProperty(value = "进件号")
    private String loanNo;
    @ApiModelProperty(value = "协议总状态:1：草拟中，2：已无效，3：审核中，4：审核通过，5：已盖章，6：已签约，7：补充中，8：解约中，9：变更中，10：已解约，11：已完结，12：已归档，13：已驳回,14 盖章中")
    private String status;
    @ApiModelProperty(value = "借款金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "合同操作流程")
    private Integer inputType;
    @ApiModelProperty(value = "产品编号")
    private String productNo;
    @ApiModelProperty(value = "城市名")
    private String cityName;
    @ApiModelProperty(value = "用户code列表")
    private List<Long> userCodeList;
    @ApiModelProperty(value = "查询页")
    private Integer pageNo;
    @ApiModelProperty(value = "每页格式")
    private Integer pageSize;
    @ApiModelProperty(value = "时间搜索顺序")
    private  String searchTimeFlag;
    @ApiModelProperty(value = "合同状态集合")
    private  List<String> statusList;
    @ApiModelProperty(value = "搜索关键字")
    private  String searchKey;
}
