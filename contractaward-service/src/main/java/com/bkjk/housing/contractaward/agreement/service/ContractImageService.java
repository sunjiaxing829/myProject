package com.bkjk.housing.contractaward.agreement.service;

import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.common.enums.BusinessTypeEnum;
import com.bkjk.housing.common.enums.LoanStatusEnum;
import com.bkjk.housing.common.thirdcenter.finance.FinanceService;
import com.bkjk.housing.common.thirdcenter.product.ProductService;
import com.bkjk.housing.common.util.BeanCopyUtil;
import com.bkjk.housing.contractaward.agreement.api.AgreementApi;
import com.bkjk.housing.contractaward.agreement.api.ContractImageApi;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractImageBo;
import com.bkjk.housing.contractaward.agreement.vo.ContractImageVo;
import com.bkjk.housing.contractaward.contract.domain.ContractInfoBo;
import com.bkjk.platform.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Service
public class ContractImageService {

    @Inject
    private ContractImageApi contractImageApi;

    @Inject
    private FinanceService financeService;

    @Inject
    private AgreementApi agreementApi;

    @Inject
    private AgreementService agreementService;

    @Inject
    private ProductService productService;

    public Boolean batchSaveContractImage(List<ContractImageVo> contractImageVoList) {
        List<ContractImageBo> contractImageBoList = BeanCopyUtil.copyList(contractImageVoList, ContractImageBo.class);
        AgreementBo operateAgreementBo = this.agreementApi.getAgreementByNo(contractImageVoList.get(0).getAgreementNo());
        ContractInfoBo contractInfoBo = operateAgreementBo.getContractInfoBo();
        contractInfoBo.setSignTime(new Date());
        CombinationProductDetailBo combinationProductDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        if (LoanStatusEnum.RISK_CONTROL_AUDIT_ING.getCode().equals(contractInfoBo.getLoanStatus())) {
            if (BusinessTypeEnum.MAIN.getCode().equals(operateAgreementBo.getBusinessType()) || BusinessTypeEnum.REPLENISH.getCode().equals(operateAgreementBo.getBusinessType()))
                throw new BusinessException("金融单风控审核中,禁止上传电子件");
        }
        agreementService.signToExternalFlow(operateAgreementBo, contractInfoBo);
        this.contractImageApi.batchSaveContractImage(contractInfoBo.getLoanNo(), contractImageBoList);
        return Boolean.TRUE;
    }
}
