package com.bkjk.housing.contractaward.agreement.web;

import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.classic.consts.AgreementType;
import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.enums.BusinessTypeEnum;
import com.bkjk.housing.common.enums.SupplementAgreementEnum;
import com.bkjk.housing.common.enums.TerminationAgreementEnum;
import com.bkjk.housing.common.thirdcenter.contract.ContractEditorService;
import com.bkjk.housing.common.thirdcenter.finance.FinanceService;
import com.bkjk.housing.common.thirdcenter.loan.LoanService;
import com.bkjk.housing.common.thirdcenter.passport.PassportService;
import com.bkjk.housing.common.thirdcenter.product.ProductService;
import com.bkjk.housing.common.util.PrivilegeUtil;
import com.bkjk.housing.contractaward.agreement.api.AgreementApi;
import com.bkjk.housing.contractaward.agreement.domain.*;
import com.bkjk.housing.contractaward.agreement.service.AgreementService;
import com.bkjk.housing.contractaward.agreement.vo.*;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.contract.contract.domain.ContractPreviewBo;
import com.bkjk.platform.contract.contract.domain.ContractSealBo;
import com.bkjk.platform.contract.template.domain.TemplateParametricPreviewBo;
import com.bkjk.platform.contract.template.domain.TemplatePreviewBo;
import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import com.bkjk.platform.orm.mybatis.segment.PageSegment;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@LoginRequired
@Api(description = "协议相关接口")
public class AgreementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementController.class);
    @Inject
    private AgreementApi agreementApi;
    @Inject
    private LoanService loanService;
    @Inject
    private PassportService passportService;
    @Inject
    private AgreementService agreementService;
    @Inject
    private ProductService productService;
    @Inject
    private FinanceService financeService;
    @Inject
    private ContractEditorService editorService;
    @Inject
    private PrivilegeUtil privilegeUtil;

    @ApiVersion("1.0")
    @ApiOperation(value = "查询协议审核列表", httpMethod = "POST", notes = "查询协议审核列表")
    @RequestMapping(value = "/examine-agreements", method = RequestMethod.POST)
    public PageSegment<ExamineAgreementBo> getExamineAgreements(@RequestBody AgreementVo agreementVo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        AgreementSearchBo agreementSearchBo = new AgreementSearchBo();
        BeanUtils.copyProperties(agreementVo, agreementSearchBo);
        if (!privilegeUtil.checkSuperMan(loginUserBo)) {
            agreementSearchBo.setExamineUserCode(loginUserBo.getUserId());
        }
        return agreementApi.getExamineAgreements(agreementSearchBo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "编辑协议模版", httpMethod = "GET", notes = "编辑协议模版")
    @RequestMapping(value = "/agreement/{agreementNo}", method = RequestMethod.GET)
    public AgreementTemplateVo getAgreementByNo(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo) {
        return this.agreementService.getAgreementByNo(agreementNo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "解约、补充协议设置无效", httpMethod = "GET", notes = "解约、补充协议设置无效")
    @RequestMapping(value = "/invalid-agreement/{agreementNo}", method = RequestMethod.GET)
    public boolean invalidAgreement(@ApiParam("协议号") @PathVariable("agreementNo") String agreementNo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        if (BusinessTypeEnum.REPLENISH.getCode().equals(agreementBo.getBusinessType())) {
            loanService.contractSupplement(agreementBo.getLoanNo(), agreementNo, SupplementAgreementEnum.SUPPLEMENT_FAILURE);
        } else {
            loanService.contractTerminate(agreementBo.getLoanNo(), TerminationAgreementEnum.TERMINATION_FAILURE);
        }
        agreementApi.invalidAgreement(agreementNo, loginUserBo.getUserId(), loginUserBo.getUserName());
        return true;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取解约协议", httpMethod = "GET", notes = "获取解约协议")
    @RequestMapping(value = "/termination-agreement/{loanNo}", method = RequestMethod.GET)
    public AgreementTemplateVo getTerminationAgreement(@ApiParam("进件号") @PathVariable("loanNo") String loanNo) {
        return this.agreementService.produceAgreement(loanNo, AgreementType.RECISSION);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取补充协议", httpMethod = "GET", notes = "获取补充协议")
    @RequestMapping(value = "/supplemental-agreement/{loanNo}", method = RequestMethod.GET)
    public AgreementTemplateVo getSupplementalAgreement(@ApiParam("进件号") @PathVariable("loanNo") String loanNo) {
        return this.agreementService.produceAgreement(loanNo, AgreementType.REPLENISH);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "协议预览", httpMethod = "GET", notes = "协议预览")
    @RequestMapping(value = "/preview-agreement/{agreementNo}", method = RequestMethod.GET)
    public PreviewAgreementBo previewAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo, HttpServletRequest request) {
        PreviewAgreementBo previewAgreementBo = new PreviewAgreementBo();
        previewAgreementBo.setAgreementNo(agreementNo);
        LoginUserBo loginUserBo = passportService.getUser(request);
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        BeanUtils.copyProperties(agreementBo, previewAgreementBo);
        previewAgreementBo.setLoanNo(agreementBo.getContractInfoBo().getLoanNo());
        if (StringUtils.hasText(agreementBo.getCapitalPlatformUrl())) {
            previewAgreementBo.setPdfUrl(agreementBo.getCapitalPlatformUrl());
        } else if (Objects.nonNull(agreementBo.getSealTime()) && Integer.valueOf(1).equals(agreementBo.getContractInfoBo().getInputType())) {
            ContractSealBo sealBo = editorService.sealAgreement(agreementNo, true, true);
            previewAgreementBo.setPdfUrl(sealBo.getSealUrl());
        } else {
            ContractPreviewBo previewBo = editorService.previewAgreement(agreementNo, true, true);
            previewAgreementBo.setPdfUrl(previewBo.getUrl());
        }
        agreementApi.previewAgreement(agreementBo, loginUserBo.getUserId(), loginUserBo.getUserName());
        return previewAgreementBo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取待打印合同", httpMethod = "GET", notes = "获取待打印合同")
    @RequestMapping(value = "/print-agreement/{agreementNo}", method = RequestMethod.GET)
    public AgreementPdfBo printAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo) {
        AgreementPdfBo pdfBo = new AgreementPdfBo();
        pdfBo.setAgreementNo(agreementNo);
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        ContractPreviewBo previewBo = editorService.previewAgreement(agreementNo, true, false);

        if (StringUtils.hasText(agreementBo.getCapitalPlatformUrl())) {
            pdfBo.setSwfPdfUrl(agreementBo.getCapitalPlatformUrl());
        } else if (Objects.nonNull(agreementBo.getSealTime()) && Integer.valueOf(1).equals(agreementBo.getContractInfoBo().getInputType())) {
            ContractSealBo sealBo = editorService.sealAgreement(agreementNo, true, false);
            pdfBo.setSwfPdfUrl(sealBo.getSwfSealUrl());
        } else {
            //打印
            pdfBo.setSwfPdfUrl(previewBo.getSwfUrl());
        }
        //打印草稿
        pdfBo.setPdfUrl(previewBo.getUrl());
        return pdfBo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取带编号的空白合同", httpMethod = "GET", notes = "获取带编号的空白合同")
    @RequestMapping(value = "/numbered-agreement/{agreementNo}", method = RequestMethod.GET)
    public AgreementPdfBo numberedAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo) {
        AgreementPdfBo pdfBo = new AgreementPdfBo();
        pdfBo.setAgreementNo(agreementNo);
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        //带编号合同预览
        Map<String, Object> map = new HashMap<>();
        map.put("ContractNo", agreementNo);
        TemplateParametricPreviewBo parametricPreviewBo = editorService.previewParametersTemplate(agreementBo.getTemplateId(), map);
        pdfBo.setPdfUrl(parametricPreviewBo.getSwfUrl());
        return pdfBo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取空白合同", httpMethod = "GET", notes = "获取空白合同")
    @RequestMapping(value = "/blank-agreement/{agreementNo}", method = RequestMethod.GET)
    public AgreementPdfBo blankAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo) {
        AgreementPdfBo pdfBo = new AgreementPdfBo();
        pdfBo.setAgreementNo(agreementNo);
        AgreementBo agreementBo = agreementApi.getAgreementByNo(agreementNo);
        TemplatePreviewBo templatePreviewBo = editorService.previewTemplate(agreementBo.getTemplateId());
        pdfBo.setPdfUrl(templatePreviewBo.getUrl());
        return pdfBo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "协议盖章", httpMethod = "GET", notes = "协议盖章")
    @RequestMapping(value = "/seal-agreement/{agreementNo}", method = RequestMethod.GET)
    public AgreementBo sealAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        return this.agreementService.sealAgreement(agreementNo, loginUserBo);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "校验多模版相同字段值", httpMethod = "POST", notes = "校验多模版相同字段值")
    @RequestMapping(value = "/agreement/validatefieldchange", method = RequestMethod.POST)
    public Boolean validCommonFieldDetail(@RequestBody AgreementCheckVo agreementCheckVo) {
        return agreementService.validCommonFieldDetail(agreementCheckVo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "保存合同信息", httpMethod = "PUT", notes = "保存合同信息")
    @RequestMapping(value = "/save-agreement", method = RequestMethod.PUT)
    public AgreementTemplateVo saveContractAgreementDetail(@RequestBody AgreementSaveVo agreementSaveVo) {
        AgreementTemplateVo agreementTemplateVo = new AgreementTemplateVo();
        AgreementBo agreementBo = agreementService.saveContractAgreement(agreementSaveVo);
        agreementTemplateVo.setHtml(editorService.getAgreementHtml(agreementBo.getTemplateId(), agreementSaveVo.getDetailJsonObject()));
        agreementTemplateVo.setBusinessType(agreementBo.getBusinessType());
        agreementTemplateVo.setAgreementName(agreementBo.getAgreementName());
        agreementTemplateVo.setAgreementNo(agreementBo.getAgreementNo());
        agreementTemplateVo.setProductType(agreementBo.getContractInfoBo().getProductType());
        agreementTemplateVo.setTemplateId(agreementBo.getTemplateId());
        agreementTemplateVo.setTemplateType(agreementBo.getTemplateType());
        return agreementTemplateVo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "提交合同信息", httpMethod = "PUT", notes = "提交合同信息")
    @RequestMapping(value = "/commit-agreement", method = RequestMethod.PUT)
    public AgreementTemplateVo commitContractAgreement(@RequestBody AgreementSaveVo agreementSaveVo) {
        AgreementTemplateVo agreementTemplateVo = new AgreementTemplateVo();
        AgreementBo agreementBo = agreementService.commitContractAgreement(agreementSaveVo);
        agreementTemplateVo.setHtml(editorService.getAgreementHtml(agreementBo.getTemplateId(), agreementSaveVo.getDetailJsonObject()));
        agreementTemplateVo.setBusinessType(agreementBo.getBusinessType());
        agreementTemplateVo.setAgreementName(agreementBo.getAgreementName());
        agreementTemplateVo.setAgreementNo(agreementBo.getAgreementNo());
        agreementTemplateVo.setProductType(agreementBo.getContractInfoBo().getProductType());
        agreementTemplateVo.setTemplateId(agreementBo.getTemplateId());
        agreementTemplateVo.setTemplateType(agreementBo.getTemplateType());
        return agreementTemplateVo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "主协议确认签约", httpMethod = "PUT", notes = "主协议确认签约(非北京/燕京)")
    @RequestMapping(value = "/signed-agreement/{agreementNo}", method = RequestMethod.PUT)
    public Boolean signedAgreement(@PathVariable("agreementNo") String agreementNo) {
        return agreementService.signedAgreement(agreementNo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "计算-担保类（计算标准担保服务费）", httpMethod = "POST", notes = "计算-担保类（计算标准担保服务费）")
    @RequestMapping(value = "/calculate", method = RequestMethod.POST)
    public String calculateStandardCharge(@RequestBody CalculateVo calculateVo) {
        CombinationProductDetailBo combinationProductDetailBo = this.productService.getProductDetailByNo(calculateVo.getProductNo());
        if ("Y".equals(combinationProductDetailBo.getSkipFinance()))
            throw new BusinessException("该产品【" + calculateVo.getProductNo() + "】跳过财务计费规则!");
        return financeService.caculateStandardCharge(calculateVo);
    }
}
