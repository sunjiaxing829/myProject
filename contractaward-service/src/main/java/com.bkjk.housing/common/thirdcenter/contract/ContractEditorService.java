package com.bkjk.housing.common.thirdcenter.contract;

import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.platform.contract.contract.api.ContractApi;
import com.bkjk.platform.contract.contract.domain.*;
import com.bkjk.platform.contract.template.api.TemplateApi;
import com.bkjk.platform.contract.template.api.TemplateMetaApi;
import com.bkjk.platform.contract.template.domain.*;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 合同模版编辑器
 */
@Component
public class ContractEditorService {

    private final Logger logger = LoggerFactory.getLogger(ContractEditorService.class);

    @Inject
    private ContractApi contractApi;

    @Inject
    private TemplateMetaApi templateMetaApi;

    @Inject
    private TemplateApi templateApi;

    public ContractBo saveOrUpdateFile(AgreementBo agreementBo, Map<String, Object> fileJson) {
        ContractBo contractBo = getAgreementFieldByNo(agreementBo.getAgreementNo());
        if (Objects.isNull(contractBo)) {
            ContractCreateBo contractCreateBo = new ContractCreateBo();
            contractCreateBo.setParameters(JSONConvert.toString(fileJson, Map.class));
            contractCreateBo.setContractNo(agreementBo.getAgreementNo());
            contractCreateBo.setTemplateVersionId(agreementBo.getTemplateId());
            logger.info("【合同中心】[协议模版保存] request -> {},{}", agreementBo.getAgreementNo(), JSONConvert.toString(contractCreateBo));
            contractBo = this.contractApi.create(contractCreateBo);
            logger.info("【合同中心】[协议模版保存] response -> {},{}", agreementBo.getAgreementNo(), Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        } else {
            ContractUpdateBo contractUpdateBo = new ContractUpdateBo();
            contractUpdateBo.setParameters(JSONConvert.toString(fileJson, Map.class));
            contractUpdateBo.setContractNo(agreementBo.getAgreementNo());
            logger.info("【合同中心】[协议模版修改] request -> {},{}", agreementBo.getAgreementNo(), JSONConvert.toString(contractUpdateBo));
            contractBo = this.contractApi.update(contractUpdateBo);
            logger.info("【合同中心】[协议模版修改] response -> {},{}", agreementBo.getAgreementNo(), Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        }
        return contractBo;
    }

    /**
     * 保存合同详情
     */
    public ContractBo saveFeild(AgreementBo agreementBo, Map<String, Object> fileJson) {
        ContractCreateBo contractCreateBo = new ContractCreateBo();
        contractCreateBo.setParameters(JSONConvert.toString(fileJson, Map.class));
        contractCreateBo.setContractNo(agreementBo.getAgreementNo());
        contractCreateBo.setTemplateVersionId(agreementBo.getTemplateId());
        logger.info("【合同中心】[协议模版保存] request -> {}", JSONConvert.toString(contractCreateBo));
        ContractBo contractBo = this.contractApi.create(contractCreateBo);
        logger.info("【合同中心】[协议模版保存] response -> {}", Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        return contractBo;
    }


    public ContractBo updateFeild(AgreementBo agreementBo, Map<String, Object> fileJson) {
        ContractUpdateBo contractUpdateBo = new ContractUpdateBo();
        contractUpdateBo.setParameters(JSONConvert.toString(fileJson, Map.class));
        contractUpdateBo.setContractNo(agreementBo.getAgreementNo());
        logger.info("【合同中心】[协议模版修改] request -> {}", JSONConvert.toString(contractUpdateBo));
        ContractBo contractBo = this.contractApi.update(contractUpdateBo);
        logger.info("【合同中心】[协议模版修改] response -> {}", Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        return contractBo;
    }


    /**
     * 协议渲染
     *
     * @param templateId
     * @param templateFieldMap
     * @return
     */
    public String getAgreementHtml(Long templateId, Map<String, Object> templateFieldMap) {
        logger.info("【合同中心】[协议渲染] request -> {},{}", templateId, JSONConvert.toString(templateFieldMap));
        TemplateRenderBo templateRenderBo = new TemplateRenderBo();
        templateRenderBo.setParameters(templateFieldMap);
        TemplateRenderableBo templateRenderableBo = templateMetaApi.render(templateId, templateRenderBo);
        logger.info("【合同中心】[协议渲染] response -> {},{}", templateId, Objects.isNull(templateRenderableBo) ? null : JSONConvert.toString(templateRenderableBo));
        return templateRenderableBo.getContent();
    }

    /**
     * 获取模版信息
     *
     * @param templateId
     * @return
     */
    public AggregatedTemplateBo getTemplateById(Long templateId) {
        logger.info("【合同中心】[获取模版信息] request -> {}", templateId);
        AggregatedTemplateBo templateBo = templateApi.getAggregatedVersionedTemplate(templateId);
        logger.info("【合同中心】[获取模版信息] response -> {},{}", templateId, Objects.isNull(templateBo) ? null : JSONConvert.toString(templateBo));
        if (Objects.isNull(templateBo)) throw new BusinessException("模板id【" + templateId + "】不存在！");
        return templateBo;
    }


    /**
     * 模版预览
     *
     * @param templateId
     * @return
     */
    public TemplatePreviewBo previewTemplate(Long templateId) {
        logger.info("【合同中心】[模版预览] request -> {}", templateId);
        TemplatePreviewBo templatePreviewBo = templateMetaApi.preview(templateId);
        logger.info("【合同中心】[模版预览] response -> {},{}", templateId, Objects.isNull(templatePreviewBo) ? null : JSONConvert.toString(templatePreviewBo));
        return templatePreviewBo;
    }

    /**
     * 模版带参数预览
     *
     * @param templateId
     * @return
     */
    public TemplateParametricPreviewBo previewParametersTemplate(Long templateId, Map<String, Object> parametersMap) {
        logger.info("【合同中心】[模版带参数预览] request -> {},{}", templateId, JSONConvert.toString(parametersMap));
        TemplateParameterPreviewBo parameterPreviewBo = new TemplateParameterPreviewBo();
        parameterPreviewBo.setParameters(parametersMap);
        TemplateParametricPreviewBo parametricPreviewBo = templateMetaApi.previewParameters(templateId, parameterPreviewBo);
        logger.info("【合同中心】[模版带参数预览] response -> {},{}", templateId, Objects.isNull(parametricPreviewBo) ? null : JSONConvert.toString(parametricPreviewBo));
        return parametricPreviewBo;
    }

    /**
     * 协议预览
     *
     * @param agreementNo
     * @return
     */
    public ContractPreviewBo previewAgreement(String agreementNo, boolean canPlaceholder, boolean canRedPlaceholder) {
        logger.info("【合同中心】[协议预览] request -> {}", agreementNo);
        ContractPreviewBo previewBo = contractApi.preview(agreementNo, canPlaceholder, canRedPlaceholder);
        logger.info("【合同中心】[协议预览] response -> {},{}", agreementNo, Objects.isNull(previewBo) ? null : JSONConvert.toString(previewBo));
        return previewBo;
    }

    /**
     * 协议盖章
     *
     * @param agreementNo
     * @return
     */
    public ContractSealBo sealAgreement(String agreementNo, boolean canPlaceholder, boolean red) {
        logger.info("【合同中心】[协议盖章] request -> {}", agreementNo);
        ContractSealBo contractSealBo = contractApi.seal(agreementNo, canPlaceholder, red);
        logger.info("【合同中心】[协议盖章] response -> {},{}", agreementNo, Objects.isNull(contractSealBo) ? null : JSONConvert.toString(contractSealBo));
        return contractSealBo;
    }


    /**
     * 获取单个协议模版填充
     *
     * @param agreementNo
     * @return
     */
    public ContractBo getAgreementFieldByNo(String agreementNo) {
        logger.info("【合同中心】[单个获取协议模版填充信息] request -> {}", agreementNo);
        ContractBo contractBo = contractApi.getByContractNo(agreementNo);
        logger.info("【合同中心】[单个获取协议模版填充信息] response -> {},{}", agreementNo, Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        return contractBo;
    }

    /**
     * 批量获取协议模版填充信息
     *
     * @param agreementNoList
     * @return
     */
    public List<ContractBo> getAgreementFieldsByNos(List<String> agreementNoList) {
        if (CollectionUtils.isEmpty(agreementNoList)) {
            return null;
        }
        ContractQueryBo contractQueryBo = new ContractQueryBo();
        contractQueryBo.setContractNos(agreementNoList);
        logger.info("【合同中心】[批量获取协议模版填充信息] request -> {}", JSONConvert.toString(contractQueryBo));
        List<ContractBo> contractBoList = this.contractApi.query(contractQueryBo);
        logger.info("【合同中心】[批量获取协议模版填充信息] response -> {}", CollectionUtils.isEmpty(contractBoList) ? null : JSONConvert.toString(contractBoList));
        return contractBoList;
    }

    public Map<String, JSONObject> getAgreementJsonMap(List<String> agreementNoList) {
        List<ContractBo> contractBoList = this.getAgreementFieldsByNos(agreementNoList);
        if (CollectionUtils.isEmpty(contractBoList)) {
            throw new BusinessException("合同模版不存在 agreementNoList -> " + JSONConvert.toString(agreementNoList));
        }
        Map<String, JSONObject> compareMap = new HashedMap();
        contractBoList.stream().forEach(item -> {
            JSONObject jsonObject = JSONObject.fromObject(item.getParameters());
            compareMap.put(item.getContractNo(), jsonObject);
        });
        return compareMap;
    }

    public Map<String, Object> getAgreementFieldMap(String agreementNo) {
        logger.info("【合同中心】[获取协议模版填充信息] request -> {}", agreementNo);
        ContractBo contractBo = this.contractApi.getByContractNo(agreementNo);
        logger.info("【合同中心】[获取协议模版填充信息] response -> {}, {}", agreementNo, Objects.isNull(contractBo) ? null : JSONConvert.toString(contractBo));
        return JSONConvert.fromString(Map.class, contractBo.getParameters());
    }


}
