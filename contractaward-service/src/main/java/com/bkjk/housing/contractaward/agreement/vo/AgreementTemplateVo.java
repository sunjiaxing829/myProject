package com.bkjk.housing.contractaward.agreement.vo;

import lombok.Data;

@Data
public class AgreementTemplateVo {
    private String html;
    private String agreementNo;
    private String agreementName;
    private String businessType;
    private String productType;
    private Long templateId;
    private String templateType;
}
