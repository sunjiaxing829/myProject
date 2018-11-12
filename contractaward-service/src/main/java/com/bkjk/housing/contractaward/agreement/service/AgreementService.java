package com.bkjk.housing.contractaward.agreement.service;

import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.classic.combination.api.domain.FeeItemBo;
import com.bkjk.housing.classic.consts.AgreementType;
import com.bkjk.housing.classic.contract.api.domain.CombinationContractBo;
import com.bkjk.housing.common.enums.*;
import com.bkjk.housing.common.thirdcenter.capitalplatform.CapitalPlatformService;
import com.bkjk.housing.common.thirdcenter.capitalplatform.domain.FundInfoResponseBo;
import com.bkjk.housing.common.thirdcenter.contract.ContractEditorService;
import com.bkjk.housing.common.thirdcenter.crm.CrmService;
import com.bkjk.housing.common.thirdcenter.finance.FinanceService;
import com.bkjk.housing.common.thirdcenter.loan.LoanService;
import com.bkjk.housing.common.thirdcenter.product.ProductService;
import com.bkjk.housing.common.util.BeanCopyUtil;
import com.bkjk.housing.contractaward.agreement.api.AgreementApi;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.agreement.domain.AuditAgreementBo;
import com.bkjk.housing.contractaward.agreement.domain.FundInformationBo;
import com.bkjk.housing.contractaward.agreement.vo.AgreementCheckVo;
import com.bkjk.housing.contractaward.agreement.vo.AgreementSaveVo;
import com.bkjk.housing.contractaward.agreement.vo.AgreementTemplateVo;
import com.bkjk.housing.contractaward.common.api.CommonApi;
import com.bkjk.housing.contractaward.common.domain.FieldMappingBo;
import com.bkjk.housing.contractaward.contract.api.ContractInfoApi;
import com.bkjk.housing.contractaward.contract.domain.ContractInfoBo;
import com.bkjk.housing.contractaward.examine.api.AgreementExamineApi;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.contract.contract.domain.ContractBo;
import com.bkjk.platform.contract.template.domain.AggregatedTemplateBo;
import com.bkjk.platform.contract.template.entity.TemplateType;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgreementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementService.class);
    @Inject
    private ContractEditorService contractEditorService;

    @Inject
    private FinanceService financeService;

    @Inject
    private AgreementApi agreementApi;

    @Inject
    private AgreementExamineApi agreementExamineApi;

    @Inject
    private LoanService loanService;

    @Inject
    private ContractInfoApi contractInfoApi;

    @Inject
    private ProductService productService;

    @Inject
    private CommonApi commonApi;

    @Inject
    private CapitalPlatformService capitalPlatformService;

    @Inject
    private ContractEditorService editorService;

    @Inject
    private CrmService crmService;

    /**
     * 保存协议
     *
     * @param agreementSaveVo
     * @return
     */
    public AgreementBo saveContractAgreement(AgreementSaveVo agreementSaveVo) {
        AgreementBo agreementBo = BeanCopyUtil.copy(agreementSaveVo, AgreementBo.class);
        AgreementBo operateAgreementBo = this.agreementApi.saveAgreement(agreementBo);
        contractEditorService.saveOrUpdateFile(operateAgreementBo, agreementSaveVo.getDetailJsonObject());
        ContractInfoBo contractInfoBo = operateAgreementBo.getContractInfoBo();
        operateAgreementBo.setDetailJsonObject(agreementSaveVo.getDetailJsonObject());
        if (BusinessTypeEnum.REPLENISH.getCode().equals(agreementSaveVo.getBusinessType())) {
            loanService.contractSupplement(contractInfoBo.getLoanNo(), operateAgreementBo.getAgreementNo(), SupplementAgreementEnum.SUPPLEMENT_START);
        } else if (BusinessTypeEnum.RECISSION.getCode().equals(agreementSaveVo.getBusinessType())) {
            loanService.contractTerminate(contractInfoBo.getLoanNo(), TerminationAgreementEnum.TERMINATION_START);
        }
        return operateAgreementBo;
    }

    /**
     * 提交协议
     *
     * @param agreementSaveVo
     * @return
     */
    public AgreementBo commitContractAgreement(AgreementSaveVo agreementSaveVo) {
        AgreementBo agreementBo = BeanCopyUtil.copy(agreementSaveVo, AgreementBo.class);
        if (StringUtils.isEmpty(agreementSaveVo.getAgreementNo())) throw new BusinessException("合同协议不存在，请先保存再提交!");
        AgreementBo operateAgreementBo = this.agreementApi.getAgreementByNo(agreementSaveVo.getAgreementNo());
        this.checkAgreementStatus(operateAgreementBo);
        contractEditorService.saveOrUpdateFile(operateAgreementBo, agreementSaveVo.getDetailJsonObject());
        operateAgreementBo.setDetailJsonObject(agreementSaveVo.getDetailJsonObject());
        if (BusinessTypeEnum.MAIN.getCode().equals(operateAgreementBo.getBusinessType())) {
            //保存资金，担保，居间表
            this.contractInfoApi.saveContractFinance(agreementSaveVo.getDetailJsonObject(), operateAgreementBo.getAgreementNo());
            //获取产品
            CombinationProductDetailBo combinationProductDetailBo = this.productService.getProductDetailByNo(operateAgreementBo.getContractInfoBo().getProductNo());
            //推计费
            calculateFinance(operateAgreementBo, combinationProductDetailBo);
            //判断是否推台账
            ContractInfoBo contractInfoBo = operateAgreementBo.getContractInfoBo();
            Integer notCommitSum = this.agreementApi.getNotCommitNum(contractInfoBo.getId(), operateAgreementBo.getAgreementNo(), BusinessTypeEnum.MAIN.getCode());
            if (notCommitSum == 0 && !"110000".equals(contractInfoBo.getCityCode()) && !"131082".equals(contractInfoBo.getCityCode())) {
                // 异地&&最后一次提交(提审)
                auditToFinance(operateAgreementBo, combinationProductDetailBo);
            }
        }
        this.agreementApi.commitAgreement(agreementBo);
        return operateAgreementBo;
    }


    /**
     * 提审协议
     *
     * @param auditAgreementBo
     * @return
     */
    public boolean auditAgreement(AuditAgreementBo auditAgreementBo) {
        AgreementBo agreementBo = agreementApi.validateAuditAgreement(auditAgreementBo.getAgreementNo());
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        if (BusinessTypeEnum.MAIN.getCode().equals(agreementBo.getBusinessType())) {
            //获取产品
            CombinationProductDetailBo combinationProductDetailBo = this.productService.getProductDetailByNo(contractInfoBo.getProductNo());
            //推台账
            auditToFinance(agreementBo, combinationProductDetailBo);
        }
        auditAgreementBo.setLoanNo(agreementBo.getLoanNo());
        auditAgreementBo.setBusinessType(agreementBo.getBusinessType());
        auditAgreementBo.setAgreementName(agreementBo.getAgreementName());
        auditAgreementBo.setContractId(agreementBo.getContractId());
        auditAgreementBo.setProductType(contractInfoBo.getProductType());
        auditAgreementBo.setDiscount(contractInfoBo.getDiscount());
        return agreementExamineApi.auditAgreement(auditAgreementBo);
    }

    /**
     * 异地签约（等于上传）
     *
     * @param agreementNo
     * @return
     */
    public Boolean signedAgreement(String agreementNo) {
        AgreementBo operateAgreementBo = this.agreementApi.getAgreementByNo(agreementNo);
        ContractInfoBo contractInfoBo = operateAgreementBo.getContractInfoBo();
        contractInfoBo.setSignTime(new Date());
        if (LoanStatusEnum.RISK_CONTROL_AUDIT_ING.getCode().equals(contractInfoBo.getLoanStatus())) {
            if (BusinessTypeEnum.MAIN.getCode().equals(operateAgreementBo.getBusinessType()) || BusinessTypeEnum.REPLENISH.getCode().equals(operateAgreementBo.getBusinessType()))
                throw new BusinessException("金融单风控审核中,禁止上传电子件");
        }
        signToExternalFlow(operateAgreementBo, contractInfoBo);
        this.agreementApi.signedAgreement(agreementNo);
        return Boolean.TRUE;
    }


    /**
     * 提审调台账流程
     */
    private void auditToFinance(AgreementBo agreementBo, CombinationProductDetailBo combinationProductDetailBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        List<FeeItemBo> feeItemBoList = this.productService.getFeeItemBoList(contractInfoBo.getProductNo());

        if (ProductTypeEnum.SERVICE.getCode().equals(contractInfoBo.getProductType())) { //服务类
            Map<String, Object> fieldValueMap = this.contractEditorService.getAgreementFieldMap(agreementBo.getAgreementNo());
            this.financeService.auditToFinaceServiceNew(agreementBo.getAgreementNo(), contractInfoBo, feeItemBoList, combinationProductDetailBo, fieldValueMap);
        } else if (CollectionUtils.isNotEmpty(feeItemBoList) && "Y".equals(combinationProductDetailBo.getSkipFinance())) { //有费用项+跳过计费(资金+居间)
            Map<String, Object> fieldValueMap = this.contractEditorService.getAgreementFieldMap(agreementBo.getAgreementNo());
            if (ProductTypeEnum.GUARANTEE.getCode().equals(contractInfoBo.getProductType())) { //担保类
                this.financeService.auditToFinanceGuaranteeNew(agreementBo.getAgreementNo(), contractInfoBo, feeItemBoList, combinationProductDetailBo, fieldValueMap);
            } else if (ProductTypeEnum.FUND.getCode().equals(contractInfoBo.getProductType())) { //资金类
                this.financeService.auditToFinaceFundNew(agreementBo.getAgreementNo(), contractInfoBo, feeItemBoList, combinationProductDetailBo, fieldValueMap);
            }
        } else if ("N".equals(combinationProductDetailBo.getSkipFinance())) { //没有费用项 || 不跳过计费
            if (ProductTypeEnum.FUND.getCode().equals(contractInfoBo.getProductType())) {
                this.financeService.auditFundToFinance(agreementBo);
            } else if (ProductTypeEnum.GUARANTEE.getCode().equals(contractInfoBo.getProductType())) {
                this.financeService.auditGuaranteeToFinance(agreementBo);
            } else if (ProductTypeEnum.INTERMEDIARY.getCode().equals(contractInfoBo.getProductType())) {
                this.financeService.auditIntermediaryToFinance(agreementBo);
            }
        }
    }

    /**
     * 检测协议状态
     *
     * @param agreementBo
     */
    private void checkAgreementStatus(AgreementBo agreementBo) {
        if (agreementBo == null) throw new BusinessException("合同协议不存在，请先保存再提交!");
        if (ContractStatusEnum.SIGNED.getStatus().equals(agreementBo.getAgreementStatus())) {
            throw new BusinessException("该协议已签约，不可修改！");
        }
        if (ContractStatusEnum.FINNISH.getStatus().equals(agreementBo.getAgreementStatus())) {
            throw new BusinessException("该协议已完结，不可修改！");
        }
    }


    /**
     * 提交调计费流程
     *
     * @param agreementBo
     */
    private void calculateFinance(AgreementBo agreementBo, CombinationProductDetailBo combinationProductDetailBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        if (ProductTypeEnum.SERVICE.getCode().equals(contractInfoBo.getProductType()) || "Y".equals(combinationProductDetailBo.getSkipFinance()))
            return;
        List<AgreementBo> agreementBoList = this.agreementApi.getAgreementByLoanNoAndType(contractInfoBo.getId(), BusinessTypeEnum.MAIN.getCode());
        // 计算费用
        List<String> agreementNoList = agreementBoList.stream().map(AgreementBo::getAgreementNo).collect(Collectors.toList());
        if (ProductTypeEnum.FUND.getCode().equals(contractInfoBo.getProductType())) {
            this.financeService.calculateAgreementFund(agreementBo, agreementNoList);
        } else if (ProductTypeEnum.GUARANTEE.getCode().equals(contractInfoBo.getProductType())) {
            this.financeService.calculateAgreementGuarantee(agreementBo);
        } else if (ProductTypeEnum.INTERMEDIARY.getCode().equals(contractInfoBo.getProductType())) {
            this.financeService.calculateAgreementIntermediary(agreementBo);
        }
    }


    public Boolean validCommonFieldDetail(AgreementCheckVo agreementCheckVo) {
        if (agreementCheckVo.getContractId() == null) {
            throw new BusinessException("请传入合同id");
        }
        if (Objects.nonNull(agreementCheckVo.getDetailJsonObject())) {
            throw new BusinessException("请传入详情内容");
        }
        List<AgreementBo> agreementBoList = this.agreementApi.getAgreementByLoanNoAndType(agreementCheckVo.getContractId(), BusinessTypeEnum.MAIN.getCode());
        if (CollectionUtils.isEmpty(agreementBoList)) {
            throw new BusinessException("合同协议不存在");
        }
        List<String> agreementNoList = agreementBoList.stream().filter(item -> !agreementCheckVo.getAgreementNo().equals(item.getAgreementNo())).map(AgreementBo::getAgreementNo).collect(Collectors.toList());
        JSONObject checkJson = JSONObject.fromObject(agreementCheckVo.getDetailJsonObject());
        Map<String, JSONObject> compareMap = this.contractEditorService.getAgreementJsonMap(agreementNoList);
        StringBuilder sbr = new StringBuilder();
        for (AgreementBo agreement : agreementBoList) {
            Boolean hasNotEqual = Boolean.FALSE;
            JSONObject comparedJson = compareMap.get(agreement.getAgreementNo());
            if (comparedJson != null) {
                for (Object checkKey : checkJson.keySet()) {
                    String checkValue = (String) checkJson.get(checkKey);
                    String compareValue = (String) checkJson.get(checkKey);
                    if (!checkKey.equals("ContractNo") && !checkKey.equals("ProviderContractNo") && StringUtils.isNotEmpty(checkValue) && checkValue.equals(compareValue)) {
                        hasNotEqual = Boolean.TRUE;
                        break;
                    }
                }
            }
            if (hasNotEqual) {
                sbr.append("【").append(agreement.getAgreementName()).append("】");
                continue;
            }
        }
        if (sbr.length() != 0) {
            sbr.insert(0, "合同模板相同内容发生修改，请确认，并重新提交");
            throw new BusinessException(sbr.toString());
        }
        return Boolean.TRUE;
    }

    /**
     * 签约调外部流程
     *
     * @param operateAgreementBo
     * @param contractInfoBo
     */
    public void signToExternalFlow(AgreementBo operateAgreementBo, ContractInfoBo contractInfoBo) {
        if (BusinessTypeEnum.MAIN.getCode().equals(operateAgreementBo.getBusinessType())) {
            this.financeService.signToFinance(operateAgreementBo);
            loanService.contractmain(contractInfoBo.getLoanNo(), contractInfoBo.getProductType());
        } else if (BusinessTypeEnum.REPLENISH.getCode().equals(operateAgreementBo.getBusinessType())) {
            loanService.contractSupplement(contractInfoBo.getLoanNo(), operateAgreementBo.getAgreementNo(), SupplementAgreementEnum.SUPPLEMENT_SUCCESS);
        } else if (BusinessTypeEnum.RECISSION.getCode().equals(operateAgreementBo.getBusinessType())) {
            loanService.contractTerminate(contractInfoBo.getLoanNo(), TerminationAgreementEnum.TERMINATION_SUCCESS);
            crmService.changeContract(contractInfoBo.getLoanNo(), contractInfoBo.getDemandId(), 2);
        }
    }

    /**
     * 判断map的值是不是都为null
     *
     * @param map
     * @return
     */
    public boolean validateValueNull(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Objects.nonNull(entry.getValue())) return false;
        }
        return true;
    }

    /**
     * 合同模版用进件字段回显
     *
     * @param templateFieldMap
     * @param editorCodes
     * @param loanNo
     */
    public void convertTemplateValueByLoanInfo(Map<String, Object> templateFieldMap, List<String> editorCodes, String loanNo) {
        if (!org.springframework.util.CollectionUtils.isEmpty(editorCodes)) {
            Map<String, String> fieldMap = commonApi.getFieldMappingsByEditorCodes(editorCodes).stream().collect(Collectors.toMap(FieldMappingBo::getEditorCode, FieldMappingBo::getLoanCode));
            Map<String, String> loanMap = loanService.getLoanFields(loanNo);
            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                String loanValue = "null".equalsIgnoreCase(loanMap.get(entry.getValue())) ? null : loanMap.get(entry.getValue());
                if (Objects.isNull(templateFieldMap.get(entry.getKey()))) {
                    templateFieldMap.put(entry.getKey(), loanValue);
                }
            }
        }
    }

    /**
     * 编号回显
     *
     * @param agreementBo
     * @param templateFieldMap
     */
    public void convertAgreementNo(AgreementBo agreementBo, Map<String, Object> templateFieldMap) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        if (BusinessTypeEnum.MAIN.getCode().equals(agreementBo.getBusinessType())) {
            if (contractInfoBo.getInputType() == 1) {
                //线上签约
                if (TemplateType.LOAN.getCode().equals(agreementBo.getTemplateType())) {
                    if (templateFieldMap.containsKey("applyContractNo")) {
                        templateFieldMap.put("applyContractNo", agreementBo.getAgreementNo());
                    }
                    if (templateFieldMap.containsKey("ProviderContractNo")) {
                        templateFieldMap.put("ProviderContractNo", agreementBo.getAgreementNo());
                    }
                } else if (TemplateType.MORTGAGE.getCode().equals(agreementBo.getTemplateType())) {
                    if (templateFieldMap.containsKey("ZHDYContractNo")) {
                        templateFieldMap.put("ZHDYContractNo", agreementBo.getAgreementNo());
                    }
                } else {
                    if (templateFieldMap.containsKey("ContractNo")) {
                        templateFieldMap.put("ContractNo", agreementBo.getAgreementNo());
                    }
                }
            } else {
                //线下
                if (TemplateType.LOAN.getCode().equals(agreementBo.getTemplateType()) || TemplateType.MORTGAGE.getCode().equals(agreementBo.getTemplateType()))
                    return;
                if (templateFieldMap.containsKey("ContractNo")) {
                    templateFieldMap.put("ContractNo", agreementBo.getAgreementNo());
                }
            }
        } else if (templateFieldMap.containsKey("ZhuContractNo")) {
            templateFieldMap.put("ZhuContractNo", agreementBo.getAgreementNo());
        }
    }

    public AgreementTemplateVo getAgreementByNo(String agreementNo) {
        AgreementTemplateVo agreementTemplateVo = new AgreementTemplateVo();
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);

        ContractBo contractBo = contractEditorService.getAgreementFieldByNo(agreementNo);
        String parameters = contractBo.getParameters();
        Map<String, Object> templateFieldMap = JSONConvert.fromString(Map.class, parameters);
        if (this.validateValueNull(templateFieldMap)) {
            LOGGER.info("进件字段回显，agreementNo: {}, map:{}", agreementNo, templateFieldMap);
            List<String> editorCodes = templateFieldMap.keySet().stream().collect(Collectors.toList());
            this.convertTemplateValueByLoanInfo(templateFieldMap, editorCodes, agreementBo.getLoanNo());
            this.convertAgreementNo(agreementBo, templateFieldMap);
        }
        agreementTemplateVo.setHtml(contractEditorService.getAgreementHtml(agreementBo.getTemplateId(), templateFieldMap));
        agreementTemplateVo.setAgreementNo(agreementNo);
        agreementTemplateVo.setBusinessType(agreementBo.getBusinessType());
        agreementTemplateVo.setAgreementName(agreementBo.getAgreementName());
        agreementTemplateVo.setProductType(agreementBo.getContractInfoBo().getProductType());
        agreementTemplateVo.setTemplateId(agreementBo.getTemplateId());
        agreementTemplateVo.setTemplateType(agreementBo.getTemplateType());
        return agreementTemplateVo;
    }

    public AgreementTemplateVo produceAgreement(String loanNo, AgreementType recission) {
        AgreementTemplateVo agreementTemplateVo = new AgreementTemplateVo();
        ContractInfoBo infoBo = agreementApi.validateTerminationAgreement(loanNo);
        CombinationContractBo contractBo = productService.getAgreementTemplate(infoBo.getProductNo(), recission.getCode());

        AggregatedTemplateBo templateBo = contractEditorService.getTemplateById(contractBo.getContractVersionId());
        Map<String, Object> templateFieldMap = templateBo.getTemplateFields().parallelStream().collect(HashMap::new, (m, v) -> m.put(v.getCode(), null), HashMap::putAll);
        List<String> editorCodes = templateFieldMap.keySet().stream().collect(Collectors.toList());
        this.convertTemplateValueByLoanInfo(templateFieldMap, editorCodes, infoBo.getLoanNo());
        agreementTemplateVo.setHtml(contractEditorService.getAgreementHtml(contractBo.getContractVersionId(), templateFieldMap));
        agreementTemplateVo.setBusinessType(recission.getCode());
        agreementTemplateVo.setAgreementName(templateBo.getVersionedTemplate().getName());
        agreementTemplateVo.setProductType(infoBo.getProductType());
        agreementTemplateVo.setTemplateId(contractBo.getContractVersionId());
        agreementTemplateVo.setTemplateType(templateBo.getVersionedTemplate().getOldType());
        return agreementTemplateVo;
    }

    public AgreementBo sealAgreement(String agreementNo, LoginUserBo loginUserBo) {
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        CombinationProductDetailBo combinationProductDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        if (Integer.valueOf(1).equals(contractInfoBo.getInputType())) {
            //线上签约
            if ("N".equals(combinationProductDetailBo.getSkipCoffers())
                    && (TemplateType.LOAN.getCode().equals(agreementBo.getTemplateType()) && "10".equals(agreementBo.getHostOrganizationCode()))) {
                //中航贷款合同
                //获取资金方信息
                String fundLoanNo = String.valueOf(System.currentTimeMillis());
                FundInfoResponseBo fundInfoResponseBo = capitalPlatformService.getFundInfo(agreementBo, combinationProductDetailBo, fundLoanNo);

                //推进件资方信息
                loanService.saveCapitalSerialNumber(agreementBo, fundInfoResponseBo.getCheckedChannelCode(), fundLoanNo);

                //资金方录入合同信息
                capitalPlatformService.getFundContractDoc(agreementBo, combinationProductDetailBo, fundLoanNo);

                FundInformationBo fundInformationBo = new FundInformationBo();
                fundInformationBo.setLoanNo(agreementBo.getLoanNo());
                fundInformationBo.setFundLoanNo(fundLoanNo);
                fundInformationBo.setCheckedChannelCode(fundInfoResponseBo.getCheckedChannelCode());
                agreementApi.addOrUpdateFunInformation(fundInformationBo);
                agreementBo.setAgreementStatus(ContractStatusEnum.SEALING.getStatus());
            } else if (TemplateType.MORTGAGE.getCode().equals(agreementBo.getTemplateType()) && "10".equals(agreementBo.getHostOrganizationCode())) {
                agreementApi.validateSealAgreement(agreementBo.getContractId(), agreementBo.getBusinessType());
                agreementBo.setSealTime(new Date());
                agreementBo.setAgreementStatus(ContractStatusEnum.SEALED.getStatus());
            } else {
                agreementApi.validateSealAgreement(agreementBo.getContractId(), agreementBo.getBusinessType());
                //提前调接口，判断是否可以盖章
                editorService.sealAgreement(agreementNo, true, false);
                agreementBo.setSealTime(new Date());
                agreementBo.setAgreementStatus(ContractStatusEnum.SEALED.getStatus());
            }
        } else {
            //线下签约
            if ("N".equals(combinationProductDetailBo.getSkipCoffers()) && TemplateType.LOAN.getCode().equals(agreementBo.getTemplateType())) {
                String fundLoanNo = String.valueOf(System.currentTimeMillis());
                FundInfoResponseBo fundInfoResponseBo = capitalPlatformService.getFundInfo(agreementBo, combinationProductDetailBo, fundLoanNo);
                //推进件资方信息
                loanService.saveCapitalSerialNumber(agreementBo, fundInfoResponseBo.getCheckedChannelCode(), fundLoanNo);

                FundInformationBo fundInformationBo = new FundInformationBo();
                fundInformationBo.setLoanNo(agreementBo.getLoanNo());
                fundInformationBo.setFundLoanNo(fundLoanNo);
                fundInformationBo.setCheckedChannelCode(fundInfoResponseBo.getCheckedChannelCode());
                agreementApi.addOrUpdateFunInformation(fundInformationBo);
                agreementBo.setSealTime(new Date());
                agreementBo.setAgreementStatus(ContractStatusEnum.SEALED.getStatus());
            } else {
                agreementBo.setSealTime(new Date());
                agreementBo.setAgreementStatus(ContractStatusEnum.SEALED.getStatus());
            }
        }
        agreementApi.sealAgreement(agreementBo, loginUserBo.getUserId(), loginUserBo.getUserName());
        return agreementBo;
    }
}
