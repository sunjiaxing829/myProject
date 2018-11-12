package com.bkjk.housing.common.thirdcenter.product;

import com.bkjk.housing.classic.combination.api.CombinationExternalApi;
import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.classic.combination.api.domain.FeeItemBo;
import com.bkjk.housing.classic.contract.api.CombinationContractApi;
import com.bkjk.housing.classic.contract.api.domain.CombinationContractBo;
import com.bkjk.housing.classic.feeitem.api.FeeItemApi;
import com.bkjk.housing.common.enums.BusinessTypeEnum;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Component
public class ProductService {
    private final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Inject
    private CombinationExternalApi combinationExternalApi;
    @Inject
    private CombinationContractApi combinationContractApi;
    @Inject
    private FeeItemApi feeItemApi;


    /**
     * 获取其他协议模版
     *
     * @param productNo
     * @param agreementType
     * @return
     */
    public CombinationContractBo getAgreementTemplate(String productNo, String agreementType) {
        logger.info("【产品中心】[获取其他协议模板] request -> {},{} ", productNo, agreementType);
        CombinationContractBo combinationContractBo = combinationContractApi.getOtherContract(productNo, agreementType);
        logger.info("【产品中心】[获取其他协议模板] response -> {},{}", productNo, Objects.isNull(combinationContractBo) ? null : JSONConvert.toString(combinationContractBo));
        if (Objects.isNull(combinationContractBo))
            throw new BusinessException("该产品【" + productNo + "】不存在【" + BusinessTypeEnum.getDescriptionByCode(agreementType) + "】模板！");
        return combinationContractBo;
    }

    /**
     * 获取产品详情
     *
     * @param productNo
     * @return
     */
    public CombinationProductDetailBo getProductDetailByNo(String productNo) {
        logger.info("【产品中心】[获取产品详情] request -> {}", productNo);
        CombinationProductDetailBo productDetailBo = combinationExternalApi.getCombinationProductDetailByCode(productNo);
        logger.info("【产品中心】[获取产品详情] response -> {},{}", productNo, Objects.isNull(productDetailBo) ? null : JSONConvert.toString(productDetailBo));
        if (Objects.isNull(productDetailBo)) {
            throw new BusinessException("该产品【" + productNo + "】不存在！");
        }
        return productDetailBo;
    }

    public List<FeeItemBo> getFeeItemBoList(String productNo) {
        logger.info("【产品中心】[获取费用项] request -> {}", productNo);
        List<FeeItemBo> feeItemBoList = feeItemApi.getFeeItems(productNo);
        logger.info("【产品中心】[获取费用项] response -> {},{}", productNo, Objects.isNull(feeItemBoList) ? null : JSONConvert.toString(feeItemBoList));
        if (Objects.isNull(feeItemBoList)) {
            throw new BusinessException("该产品【" + productNo + "】费用项不存在！");
        }
        return feeItemBoList;
    }
}
