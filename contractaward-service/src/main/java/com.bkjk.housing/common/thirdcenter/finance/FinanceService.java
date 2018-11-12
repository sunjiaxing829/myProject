package com.bkjk.housing.common.thirdcenter.finance;

import com.bkjk.counter.api.dto.FinanceDataCountDto;
import com.bkjk.counter.api.order.FinanceDataCounterFacade;
import com.bkjk.counter.api.order.FinanceDataInfoFacade;
import com.bkjk.finance.api.ContactAuditApi;
import com.bkjk.finance.api.ContractStatusApi;
import com.bkjk.finance.api.UnStandardContactAuditApi;
import com.bkjk.finance.apirequest.ContactAuditRequest;
import com.bkjk.finance.apirequest.UnStandardContractAuditRequest;
import com.bkjk.finance.apirequest.UnStandardContractRepaymentDto;
import com.bkjk.housing.classic.combination.api.CombinationExternalApi;
import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.classic.combination.api.domain.FeeItemBo;
import com.bkjk.housing.common.constant.MqConstants;
import com.bkjk.housing.common.enums.MessageKeyEnum;
import com.bkjk.housing.common.enums.ProductTypeEnum;
import com.bkjk.housing.common.thirdcenter.contract.ContractEditorService;
import com.bkjk.housing.common.thirdcenter.finance.domain.CalculateBo;
import com.bkjk.housing.common.thirdcenter.finance.domain.ChargeBo;
import com.bkjk.housing.common.thirdcenter.product.ProductService;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceFundBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceJujianBo;
import com.bkjk.housing.contractaward.agreement.vo.CalculateVo;
import com.bkjk.housing.contractaward.contract.api.ContractInfoApi;
import com.bkjk.housing.contractaward.contract.domain.ContractInfoBo;
import com.bkjk.platform.contract.contract.domain.ContractBo;
import com.bkjk.platform.detail.api.ChargeCalApi;
import com.bkjk.platform.detail.api.GuaranteeCalApi;
import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.dubbox.annotation.DubboxConsumer;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FinanceService {

    private final Logger logger = LoggerFactory.getLogger(FinanceService.class);

    @DubboxConsumer(check = false, timeout = 15000)
    private FinanceDataCounterFacade financeDataCounterFacade;
    @DubboxConsumer(check = false, timeout = 15000)
    private ChargeCalApi chargeCalApi;
    @DubboxConsumer(check = false, timeout = 15000)
    private GuaranteeCalApi guaranteeCalApi;
    @DubboxConsumer(check = false, timeout = 15000)
    private ContactAuditApi contactAuditApi;
    @DubboxConsumer(check = false, timeout = 15000)
    private ContractStatusApi contractStatusApi;
    @DubboxConsumer(check = false, timeout = 15000)
    private FinanceDataInfoFacade financeDataInfoFacade;
    @DubboxConsumer(check = false, timeout = 15000)
    private UnStandardContactAuditApi unStandardContactAuditApi;
    @Inject
    private RabbitTemplate rabbitTemplate;

    @Inject
    private ContractInfoApi contractInfoApi;

    @Inject
    private ProductService productService;
    @Inject
    private CombinationExternalApi combinationExternalApi;
    @Inject
    private ContractEditorService editorService;


    /**
     * 判断产品是否收费
     *
     * @param productNo
     * @return
     */
    private boolean hasFinanceCharge(String productNo) {
        Integer hasCharge;
        try {
            logger.info("【财务系统】[金融产品是否收费] request -> {} ", productNo);
            hasCharge = financeDataInfoFacade.defineIsFree(productNo);
            logger.info("【财务系统】[金融产品是否收费]<1:收费;2:不收费> response -> {},{} ", productNo, hasCharge);
        } catch (Exception e) {
            logger.error("【财务系统】[金融产品是否收费] error ->{}", productNo, e);
            throw new BusinessException("金融产品是否收费 异常, productNo:【" + productNo + "】, " + e.getMessage() + "");
        }
        return hasCharge == 2 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 保存资金类详情
     */
    private Boolean saveFundFinance(JSONObject resultJson, String loanNo, List<String> agreementNoList) {
        JSONObject data = resultJson.optJSONObject("data");
        if (resultJson.optInt("code") != 1 || data == null) {
            return Boolean.FALSE;
        }
        JSONArray chargeResListArray = data.optJSONArray("chargeResList");
        BigDecimal serviceStandFee = new BigDecimal(0);
        BigDecimal repayStandFee = new BigDecimal(0);
        BigDecimal interestFee = new BigDecimal(0);
        ContractFinanceFundBo contractFinanceFundBo = new ContractFinanceFundBo();
        contractFinanceFundBo.setLoanNo(loanNo);
        for (int i = 0; i < chargeResListArray.size(); i++) {
            JSONObject chargeResultObject = chargeResListArray.optJSONObject(i);
            Integer chargeId = chargeResultObject.optInt("chargeId");
            String standardTotalAmt = chargeResultObject.getString("standardTotalAmt");
            BigDecimal feeStandardTotal = StringUtils.hasText(standardTotalAmt) ? new BigDecimal(standardTotalAmt) : BigDecimal.valueOf(0);

            String standardRatio = chargeResultObject.getString("standardRatio");
            BigDecimal standardRate = StringUtils.hasText(standardRatio) ? new BigDecimal(standardRatio) : BigDecimal.valueOf(0);

            String disRatio = chargeResultObject.getString("disRatio");
            BigDecimal discount = StringUtils.hasText(disRatio) ? new BigDecimal(disRatio) : BigDecimal.valueOf(0);

            String receiverName = chargeResultObject.getString("receiverName");
            Integer chargeOrder = chargeResultObject.optInt("chargeOrder");
            Integer totalTerms = chargeResultObject.optInt("totalTerms");
            JSONArray jArray = chargeResultObject.optJSONArray("standardpayAmts");

            if (chargeId != null && chargeId == 1002) {
                //总管理服务费(标准)
                contractFinanceFundBo.setServiceFeeTotalStand(feeStandardTotal);
                Double firstFee = jArray.optDouble(0);
                Double secondFee = jArray.optDouble(1);
                Double thirdFee = jArray.optDouble(2);
                Double forthFee = jArray.optDouble(3);
                //第一次管理服务费（标准）
                if (!firstFee.isNaN()) {
                    contractFinanceFundBo.setFirstServiceFeeStand(new BigDecimal(firstFee));
                }
                //第二次管理服务费（标准）
                if (!secondFee.isNaN()) {
                    contractFinanceFundBo.setSecondServiceFeeStand(new BigDecimal(secondFee));
                }
                //第三次管理服务费（标准）
                if (!thirdFee.isNaN()) {
                    contractFinanceFundBo.setThirdServiceFeeStand(new BigDecimal(thirdFee));
                }
                //第四次管理服务费（标准）
                if (!forthFee.isNaN()) {
                    contractFinanceFundBo.setForthServiceFeeStand(new BigDecimal(forthFee));
                }
                //管理服务费折扣
                contractFinanceFundBo.setDiscount(discount.multiply(BigDecimal.valueOf(100)));
                //管理服务费费率（标准）
                contractFinanceFundBo.setServiceFeeRate(standardRate);
                //管理服务费收款方
                contractFinanceFundBo.setServiceReceiverName(receiverName);
                //管理服务费是否预收
                contractFinanceFundBo.setServiceChargeOrder(chargeOrder);
                //是否有总期
                if (this.checkFieldExists(agreementNoList, "PaymentPeriod")) {
                    //总期数
                    contractFinanceFundBo.setPaymentPeriod(totalTerms);
                    //第一次期数
                    contractFinanceFundBo.setFirstPaymentPeriod(totalTerms);
                }
            } else if (chargeId != null && chargeId == 1004) {
                repayStandFee = feeStandardTotal;
                //总还款保证金（标准）
                contractFinanceFundBo.setRepaymentFeeTotalStand(feeStandardTotal);
                Double firstFee = jArray.optDouble(0);
                Double secondFee = jArray.optDouble(1);
                Double thirdFee = jArray.optDouble(2);
                Double forthFee = jArray.optDouble(3);
                //第一次还款保证金（标准）
                if (!firstFee.isNaN()) {
                    contractFinanceFundBo.setFirstRepaymentFeeStand(new BigDecimal(firstFee));
                }
                //第二次还款保证金（标准）
                if (!secondFee.isNaN()) {
                    contractFinanceFundBo.setSecondRepaymentFeeStand(new BigDecimal(secondFee));
                }
                //第三次还款保证金（标准）
                if (!thirdFee.isNaN()) {
                    contractFinanceFundBo.setThirdRepaymentFeeStand(new BigDecimal(thirdFee));
                }
                //第四次还款保证金（标准）
                if (!forthFee.isNaN()) {
                    contractFinanceFundBo.setForthRepaymentFeeStand(new BigDecimal(forthFee));
                }
                //折扣
                contractFinanceFundBo.setDiscount1(discount.multiply(BigDecimal.valueOf(100)));
                //利率
                contractFinanceFundBo.setRepaymentFeeRate(standardRate);
                //收款方
                contractFinanceFundBo.setRepayReceiverName(receiverName);
                //是否预收
                contractFinanceFundBo.setRepayChargeOrder(chargeOrder);
                //是否有总期
                if (this.checkFieldExists(agreementNoList, "PaymentPeriod")) {
                    //总期数
                    contractFinanceFundBo.setPaymentPeriod(totalTerms);
                    //第一次期数
                    contractFinanceFundBo.setFirstPaymentPeriod(totalTerms);
                }
            } else if (chargeId != null && chargeId == 1006) {
                interestFee = feeStandardTotal;
                //利息
                contractFinanceFundBo.setInterestFee(feeStandardTotal);
                //利息折扣
                contractFinanceFundBo.setDiscount2(discount);
                //利息利率
                contractFinanceFundBo.setInterestFeeRate(standardRate);
                //利息收款方
                contractFinanceFundBo.setInterestReceiverName(receiverName);
                //利息是否预收
                contractFinanceFundBo.setInterestChargeOrder(chargeOrder);
            }
            BigDecimal feeTotalStand = serviceStandFee.add(repayStandFee).add(interestFee);
            //标准应收总费用
            contractFinanceFundBo.setFeeTotalStand(feeTotalStand);
        }
        this.contractInfoApi.saveOrUpdateFinnaceFun(contractFinanceFundBo);
        return Boolean.TRUE;
    }

    /**
     * 判断编辑器是否存放改字段：未保存--TRUE;已保存--FALSE
     */
    private Boolean checkFieldExists(List<String> agreementNoList, String fieldName) {
        List<ContractBo> contractBoList = this.editorService.getAgreementFieldsByNos(agreementNoList);
        if (CollectionUtils.isEmpty(contractBoList)) {
            return Boolean.TRUE;
        }
        for (ContractBo contractBo : contractBoList) {
            if (Objects.isNull(contractBo.getParameters())) continue;
            Map<String, Object> parametersMap = JSONConvert.fromString(Map.class, contractBo.getParameters());
            if (Objects.nonNull(parametersMap.get(fieldName))) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 计费-资金类
     *
     * @param agreementBo
     * @param agreementNoList
     */
    public void calculateAgreementFund(AgreementBo agreementBo, List<String> agreementNoList) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        ContractBo contractBo = editorService.getAgreementFieldByNo(agreementBo.getAgreementNo());
        CombinationProductDetailBo productDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        ContractFinanceFundBo contractFinanceFundBo = contractInfoApi.getContractFinanceFund(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceFundBo)) throw new BusinessException("请先保存合同，再提交！");
        Map<String, Object> parametersMap = JSONConvert.fromString(Map.class, contractBo.getParameters());
        CalculateBo calculateBo = new CalculateBo();
        if (Objects.nonNull(parametersMap.get("CreditorNumber"))) {
            calculateBo.setMortgageflag(Integer.valueOf(parametersMap.get("CreditorNumber").toString()));
        } else {
            calculateBo.setMortgageflag(0);
        }
        calculateBo.setQueryId(contractInfoBo.getLoanNo());
        calculateBo.setProductId(contractInfoBo.getProductNo());
        calculateBo.setProductLine(0);
        calculateBo.setProductType(ProductTypeEnum.getProductTypeIndexByName(productDetailBo.getProductUseType()));
        calculateBo.setRepayMethod(0);
        calculateBo.setDefineRepay(1);
        //借款本金
        calculateBo.setLoanAmt(contractFinanceFundBo.getBorrowingAmount());
        //借款期限
        calculateBo.setLoanDay(contractFinanceFundBo.getBorrowingTime());
        //每次缴费的期数
        List<Integer> list = new ArrayList<>();
        list.add(Objects.isNull(contractFinanceFundBo.getFirstPaymentPeriod()) ? 0 : contractFinanceFundBo.getFirstPaymentPeriod());
        list.add(Objects.isNull(contractFinanceFundBo.getSecondPaymentPeriod()) ? 0 : contractFinanceFundBo.getSecondPaymentPeriod());
        list.add(Objects.isNull(contractFinanceFundBo.getThirdPaymentPeriod()) ? 0 : contractFinanceFundBo.getThirdPaymentPeriod());
        list.add(Objects.isNull(contractFinanceFundBo.getForthPaymentPeriod()) ? 0 : contractFinanceFundBo.getForthPaymentPeriod());
        // 管理服务费
        List<ChargeBo> repayPlans = new ArrayList<>();
        if (Objects.nonNull(contractFinanceFundBo.getServiceFeeTotal())) {
            ChargeBo chargeBo = new ChargeBo();
            chargeBo.setChargeId(1002);
            chargeBo.setRealAmt(contractFinanceFundBo.getServiceFeeTotal());
            chargeBo.setTems(list);
            repayPlans.add(chargeBo);
        }
        // 还款保证金
        if (Objects.nonNull(contractFinanceFundBo.getRepaymentFeeTotal())) {
            ChargeBo chargeBo = new ChargeBo();
            chargeBo.setChargeId(1004);
            chargeBo.setRealAmt(contractFinanceFundBo.getRepaymentFeeTotal());
            chargeBo.setTems(list);
            repayPlans.add(chargeBo);
        }
        calculateBo.setRepayPlans(repayPlans);
        String param = JSONConvert.toString(calculateBo, CalculateBo.class);

        String resultJsonStr;
        try {
            logger.info("【计费系统】[计费-资金类] request -> {}, {} ", agreementBo.getLoanNo(), param);
            resultJsonStr = chargeCalApi.calAmt(param);
            logger.info("【计费系统】[计费-资金类] response -> {}, {} ", agreementBo.getLoanNo(), resultJsonStr);
        } catch (Exception e) {
            logger.info("【计费系统】[计费-资金类] error -> {} ", agreementBo.getLoanNo(), e);
            throw new BusinessException("【计费系统】[计费-资金类]失败");
        }
        if (!StringUtils.hasText(resultJsonStr)) {
            logger.info("【计费系统】[计费-资金类] error -> null ");
            throw new BusinessException("【计费系统】[计费-资金类]失败, null");
        }
        JSONObject jsonObject = JSONObject.fromObject(resultJsonStr);
        int code = jsonObject.optInt("code");
        String msg = jsonObject.optString("msg");
        if (0 == code) throw new BusinessException("【计费系统】[计费-资金类]失败, " + msg + "");
        saveFundFinance(jsonObject, contractInfoBo.getLoanNo(), agreementNoList);
    }


    /**
     * 计费-担保类（计算标准担保服务费）
     *
     * @param calculateVo
     */
    public String caculateStandardCharge(CalculateVo calculateVo) {
        if (Objects.isNull(calculateVo.getUseAmount())) throw new BusinessException("借款/担保金额不可为空");
        FinanceDataCountDto financeDataCountDto = new FinanceDataCountDto();
        financeDataCountDto.setFinanceId(calculateVo.getProductNo());
        financeDataCountDto.setContractNo(calculateVo.getLoanNo());
        financeDataCountDto.setCountType(1);
        financeDataCountDto.setSysCode(1);
        financeDataCountDto.setPayWay(1);
        financeDataCountDto.setCapital(calculateVo.getUseAmount());
        financeDataCountDto.setContractFee(calculateVo.getContractFee());
        financeDataCountDto.setDiYaShunXu(calculateVo.getHouseMortgage() == null ? 0 : calculateVo.getHouseMortgage());
        JSONObject jsonObject;
        try {
            logger.info("【计费系统】[计费-担保类（计算标准担保服务费）] request -> {}, {} ", calculateVo.getLoanNo(), JSONConvert.toString(financeDataCountDto, FinanceDataCountDto.class));
            jsonObject = financeDataCounterFacade.calculateFees(financeDataCountDto);
            logger.info("【计费系统】[计费-担保类（计算标准担保服务费）] response -> {}, {} ", calculateVo.getLoanNo(), Objects.isNull(jsonObject) ? null : JSONConvert.toString(jsonObject));
        } catch (Exception e) {
            logger.info("【计费系统】[计费-担保类（计算标准担保服务费）] error -> {} ", calculateVo.getLoanNo(), e);
            throw new BusinessException("【计费系统】[计费-担保类（计算标准担保服务费）]失败, " + e.getMessage() + "");
        }
        return jsonObject.optString("standardFee");
    }

    /**
     * 计费-担保类
     *
     * @param agreementBo
     */
    public void calculateAgreementGuarantee(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        ContractFinanceBo contractFinanceBo = contractInfoApi.getContractFinanceGuarantee(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceBo)) throw new BusinessException("请先保存合同，再提交！");
        FinanceDataCountDto financeDataCountDto = new FinanceDataCountDto();
        financeDataCountDto.setFinanceId(contractInfoBo.getProductNo());
        financeDataCountDto.setContractNo(contractInfoBo.getLoanNo());
        financeDataCountDto.setCountType(1);
        financeDataCountDto.setSysCode(1);
        financeDataCountDto.setPayWay(1);
        financeDataCountDto.setCapital(contractFinanceBo.getUseAmount());
        financeDataCountDto.setContractFee(contractFinanceBo.getGuaranteeFee());
        financeDataCountDto.setDiYaShunXu(contractFinanceBo.getHouseMortgageStatus() == null ? 0 : contractFinanceBo.getHouseMortgageStatus());
        JSONObject jsonObject;
        try {
            logger.info("【计费系统】[计费-担保类] request -> {}, {} ", contractInfoBo.getLoanNo(), JSONConvert.toString(financeDataCountDto, FinanceDataCountDto.class));
            jsonObject = financeDataCounterFacade.calculateFees(financeDataCountDto);
            logger.info("【计费系统】[计费-担保类] response -> {}, {} ", contractInfoBo.getLoanNo(), Objects.isNull(jsonObject) ? null : JSONConvert.toString(jsonObject));
        } catch (Exception e) {
            logger.info("【计费系统】[计费-担保类] error -> {}", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【计费系统】[计费-担保类]失败, " + e.getMessage() + "");
        }

        //保存担保
        String percentStr = jsonObject.optString("percent");
        BigDecimal standardFee = new BigDecimal(jsonObject.optString("standardFee"));
        BigDecimal discount = null;
        if (StringUtils.hasText(percentStr)) {
            percentStr = percentStr.substring(0, percentStr.length() - 1);
            discount = new BigDecimal(percentStr);
        }
        ContractFinanceBo operateFinanceBo = new ContractFinanceBo();
        operateFinanceBo.setLoanNo(contractInfoBo.getLoanNo());
        operateFinanceBo.setDiscount(discount);
        operateFinanceBo.setStandardFee(standardFee);
        this.contractInfoApi.saveOrUpdateContractFinanceGuarantee(operateFinanceBo);
    }


    /**
     * 计费-居间类
     *
     * @param agreementBo
     */
    public void calculateAgreementIntermediary(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        CombinationProductDetailBo productDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        ContractFinanceJujianBo contractFinanceJujianBo = contractInfoApi.getContractFinanceIntermediary(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceJujianBo)) throw new BusinessException("请先保存合同，再提交！");
        CalculateBo calculateBo = new CalculateBo();

        calculateBo.setQueryId(agreementBo.getLoanNo());
        calculateBo.setProductId(contractInfoBo.getProductNo());
        calculateBo.setProductLine(0);
        calculateBo.setProductType(ProductTypeEnum.getProductTypeIndexByName(productDetailBo.getProductUseType()));
        calculateBo.setRepayMethod(0);
        calculateBo.setDefineRepay(1);
        calculateBo.setMortgageflag(0);
        //借款本金
        calculateBo.setLoanAmt(contractFinanceJujianBo.getBorrowAmount());
        //借款期限
        calculateBo.setLoanDay(null);
        //每次缴费的期数
        List<Integer> list = new ArrayList<>();
        list.add(0);
        // 贷款服务费
        List<ChargeBo> repayPlans = new ArrayList<>();
        ChargeBo chargeBo = new ChargeBo();
        chargeBo.setChargeId(1018);
        chargeBo.setRealAmt(contractFinanceJujianBo.getLoanFee());
        chargeBo.setTems(list);
        repayPlans.add(chargeBo);
        calculateBo.setRepayPlans(repayPlans);

        String param = JSONConvert.toString(calculateBo, CalculateBo.class);

        String json;
        try {
            logger.info("【计费系统】[计费-居间类] request -> {}, {} ", agreementBo.getLoanNo(), param);
            json = guaranteeCalApi.guaranteeAmtCal(param);
            logger.info("【计费系统】[计费-居间类] response -> {}, {}  ", agreementBo.getLoanNo(), json);
        } catch (Exception e) {
            logger.info("【计费系统】[计费--居间类] error -> {}", agreementBo.getLoanNo(), e);
            throw new BusinessException("【计费系统】[计费-居间类]失败");
        }
        if (!StringUtils.hasText(json)) {
            logger.info("【计费系统】[计费--居间类] error -> null ");
            throw new BusinessException("【计费系统】[计费-居间类]失败, null");
        }
        JSONObject jsonObject = JSONObject.fromObject(json);
        int code = jsonObject.optInt("code");
        String msg = jsonObject.optString("msg");
        JSONObject data = jsonObject.optJSONObject("data");
        if (0 == code || data == null) throw new BusinessException("【计费系统】[计费-居间类]失败, " + msg + "");
        //保存居间类
        ContractFinanceJujianBo operateJujianFinanceBo = new ContractFinanceJujianBo();
        operateJujianFinanceBo.setLoanNo(agreementBo.getLoanNo());
        JSONArray jsonArray = data.optJSONArray("chargeResList");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject2 = jsonArray.optJSONObject(i);
            Integer chargeId = jsonObject2.optInt("chargeId");
            final String standardTotalAmt = jsonObject2.getString("standardTotalAmt");
            BigDecimal feeStandardTotal = StringUtils.hasText(standardTotalAmt) ? new BigDecimal(standardTotalAmt) : BigDecimal.valueOf(0);

            final String standardRatio = jsonObject2.getString("standardRatio");
            BigDecimal standardRate = StringUtils.hasText(standardRatio) ? new BigDecimal(standardRatio) : BigDecimal.valueOf(0);

            final String disRatio = jsonObject2.getString("disRatio");
            BigDecimal discount = StringUtils.hasText(disRatio) ? new BigDecimal(disRatio) : BigDecimal.valueOf(0);

            String receiverName = jsonObject2.getString("receiverName");
            Integer chargeOrder = jsonObject2.optInt("chargeOrder");
            //贷款服务费
            if (chargeId != null && chargeId == 1018) {
                operateJujianFinanceBo.setLoanFeeStand(feeStandardTotal);
                //贷款服务费折扣
                operateJujianFinanceBo.setDiscount(discount.multiply(BigDecimal.valueOf(100)));
                //贷款服务费费率（标准）
                operateJujianFinanceBo.setRate(standardRate);
                //贷款服务费收款方
                operateJujianFinanceBo.setReceiverName(receiverName);
                //贷款服务费是否预收
                operateJujianFinanceBo.setChargeOrder(chargeOrder);
            }
        }
        this.contractInfoApi.saveOrUpdateContractFinanceIntermediary(operateJujianFinanceBo);
    }

    /**
     * 资金类费用计算后组装数据
     */
    private Boolean assemJujianAfterCharge(String resultJson, JSONObject fileJson) {
        JSONObject jsonObject = JSONObject.fromObject(resultJson);
        JSONObject data = jsonObject.optJSONObject("data");
        if (Integer.valueOf((String) jsonObject.get("code")) != 1 || data == null) {
            return Boolean.FALSE;
        }
        JSONArray chargeResListArray = data.optJSONArray("chargeResList");
        for (int i = 0; i < chargeResListArray.size(); i++) {
            JSONObject jsonObject2 = chargeResListArray.optJSONObject(i);
            Integer chargeId = jsonObject2.optInt("chargeId");
            String standardTotalAmt = jsonObject2.getString("standardTotalAmt");
            BigDecimal feeStandardTotal = StringUtils.hasText(standardTotalAmt) ? new BigDecimal(standardTotalAmt) : BigDecimal.valueOf(0);

            String standardRatio = jsonObject2.getString("standardRatio");
            BigDecimal standardRate = StringUtils.hasText(standardRatio) ? new BigDecimal(standardRatio) : BigDecimal.valueOf(0);

            String disRatio = jsonObject2.getString("disRatio");
            BigDecimal discount = StringUtils.hasText(disRatio) ? new BigDecimal(disRatio) : BigDecimal.valueOf(0);

            String receiverName = jsonObject2.getString("receiverName");
            Integer chargeOrder = jsonObject2.optInt("chargeOrder");
            //贷款服务费
            if (chargeId != null && chargeId == 1018) {
                //标准应收总费用
                fileJson.put("loanServiceFeeStand", feeStandardTotal);
                //贷款服务费折扣
                fileJson.put("dicount", discount.multiply(BigDecimal.valueOf(100)));
                //贷款服务费费率（标准）
                fileJson.put("rate", standardRate);
                //贷款服务费收款方
                fileJson.put("receiverName", receiverName);
                //贷款服务费是否预收
                fileJson.put("chargeOrder", chargeOrder);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 提审推台账--资金类
     */
    public void auditFundToFinance(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        ContractBo contractBo = editorService.getAgreementFieldByNo(agreementBo.getAgreementNo());
        CombinationProductDetailBo productDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        ContractFinanceFundBo contractFinanceFundBo = contractInfoApi.getContractFinanceFund(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceFundBo)) throw new BusinessException("请先保存合同，再提交！");
        Map<String, Object> parametersMap = JSONConvert.fromString(Map.class, contractBo.getParameters());

        Map<String, Object> map = new HashMap<>();
        //抵押类型，0-无抵押；1-一抵；2-二抵
        if (Objects.nonNull(parametersMap.get("CreditorNumber"))) {
            map.put("mortgageType", Integer.valueOf(parametersMap.get("CreditorNumber").toString()));
        } else {
            map.put("mortgageType", 0);
        }

        //贷款合同编号
        if (Objects.nonNull(parametersMap.get("ProviderContractNo"))) {
            map.put("agreementNo", parametersMap.get("ProviderContractNo"));
        } else if (Objects.nonNull(parametersMap.get("applyContractNo"))) {
            map.put("agreementNo", parametersMap.get("applyContractNo"));
        } else if (Objects.nonNull(parametersMap.get("LoanContractContractNo"))) {
            map.put("agreementNo", parametersMap.get("LoanContractContractNo"));
        }
        //金融单号
        map.put("contractNo", contractInfoBo.getLoanNo());
        //交易单编号
        map.put("transactionNo", contractInfoBo.getTransactionNo());
        //金融产品id
        map.put("productNo", contractInfoBo.getProductNo());
        //产品名称
        map.put("productName", productDetailBo.getProductName());
        //产品类型
        map.put("productType", ProductTypeEnum.getProductTypeIndexByName(productDetailBo.getProductUseType()));
        //金融顾问
        map.put("adviserName", contractInfoBo.getCreatedName());
        //金融顾问系统号
        map.put("adviserId", contractInfoBo.getCreatedId());
        //借款人姓名
        map.put("borrowName", contractFinanceFundBo.getBorrowerName());
        //资金方id
        map.put("providerId", productDetailBo.getCooperationCode());
        //资金方姓名
        map.put("providerName", productDetailBo.getCooperationName());
        //地区编码
        map.put("citycode", productDetailBo.getCityCode());
        //地区名称
        map.put("cityName", productDetailBo.getCityName());
        map.put("repayMethod", 0); //还款方式
        map.put("defineRepay", 1); //是否自由定义还款节奏,1-是；2-否

        map.put("borrowingTime", contractFinanceFundBo.getBorrowingTime()); //借款期限
        map.put("borrowMoney", contractFinanceFundBo.getBorrowingAmount()); //借款金额
        map.put("signDate", contractFinanceFundBo.getSignDate()); //合同签署日期
        map.put("feeTotal", contractFinanceFundBo.getFeeTotal()); //合同应收总费用
        map.put("feeTotalStand", contractFinanceFundBo.getFeeTotalStand()); //标准应收总费用
        map.put("paymentPeriod", contractFinanceFundBo.getPaymentPeriod()); //总期数
        map.put("payMethod", contractFinanceFundBo.getPayMethod()); //分次支付方式      1,-一次性，2-分次
        map.put("payTimes", contractFinanceFundBo.getPayTimes()); //分次支付次数
        map.put("firstPaymentPeriod", contractFinanceFundBo.getFirstPaymentPeriod()); //第一次支付期数
        map.put("secondPaymentPeriod", contractFinanceFundBo.getSecondPaymentPeriod()); //第二次支付期数
        map.put("thirdPaymentPeriod", contractFinanceFundBo.getThirdPaymentPeriod()); //第三次支付期数
        map.put("forthPaymentPeriod", contractFinanceFundBo.getForthPaymentPeriod()); //第四次支付期数

        //管理服务费
        map.put("serviceFeeTotal", contractFinanceFundBo.getServiceFeeTotal()); //总管理服务费
        //map.put("eachServiceFee", contractFinance.getEachServiceFee());//每期管理服务费
        map.put("firstServiceFee", contractFinanceFundBo.getFirstServiceFee()); //第一次管理服务费
        map.put("secondServiceFee", contractFinanceFundBo.getSecondServiceFee()); //第二次管理服务费
        map.put("thirdServiceFee", contractFinanceFundBo.getThirdServiceFee()); //第三次管理服务费
        map.put("forthServiceFee", contractFinanceFundBo.getForthServiceFee()); //第四次管理服务费
        map.put("serviceFeeStandTotal", contractFinanceFundBo.getServiceFeeTotalStand()); //总管理服务费(标准)
        map.put("firstServiceFeeStand", contractFinanceFundBo.getFirstServiceFeeStand()); //第一次管理服务费(标准)
        map.put("secondServiceFeeStand", contractFinanceFundBo.getSecondServiceFeeStand()); //第二次管理服务费(标准)
        map.put("thirdServiceFeeStand", contractFinanceFundBo.getThirdServiceFeeStand()); //第三次管理服务费(标准)
        map.put("forthServiceFeeStand", contractFinanceFundBo.getForthServiceFeeStand()); //第四次管理服务费(标准)
        map.put("servicediscount", contractFinanceFundBo.getDiscount()); //折扣
        map.put("serviceFeeRate", contractFinanceFundBo.getServiceFeeRate()); //利率
        map.put("serviceReceiverName", contractFinanceFundBo.getServiceReceiverName()); //收款方
        map.put("serviceChargeOrder", contractFinanceFundBo.getServiceChargeOrder()); //是否预收


        //还款保证金
        map.put("repaymentFeeTotal", contractFinanceFundBo.getRepaymentFeeTotal()); //总还款保证金
        //map.put("eachRepaymentFee", contractFinance.getEachRepaymentFee());//每期还款保证金
        map.put("firstRepaymentFee", contractFinanceFundBo.getFirstRepaymentFee()); //第一次还款保证金
        map.put("secondRepaymentFee", contractFinanceFundBo.getSecondRepaymentFee()); //第二次还款保证金
        map.put("thirdRepaymentFee", contractFinanceFundBo.getThirdRepaymentFee()); //第三次还款保证金
        map.put("forthRepaymentFee", contractFinanceFundBo.getForthRepaymentFee()); //第四次还款保证金
        map.put("repaymentFeeStandTotal", contractFinanceFundBo.getRepaymentFeeTotalStand()); //总还款保证金(标准)
        map.put("firstRepaymentFeeStand", contractFinanceFundBo.getFirstRepaymentFeeStand()); //第一次还款保证金(标准)
        map.put("secondRepaymentFeeStand", contractFinanceFundBo.getSecondRepaymentFeeStand()); //第二次还款保证金(标准)
        map.put("thirdRepaymentFeeStand", contractFinanceFundBo.getThirdRepaymentFeeStand()); //第三次还款保证金(标准)
        map.put("forthRepaymentFeeStand", contractFinanceFundBo.getForthRepaymentFeeStand()); //第四次还款保证金(标准)
        map.put("repaydiscount", contractFinanceFundBo.getDiscount1()); //折扣
        map.put("repaymentFeeRate", contractFinanceFundBo.getRepaymentFeeRate()); //利率
        map.put("repayReceiverName", contractFinanceFundBo.getRepayReceiverName()); //收款方
        map.put("repayChargeOrder", contractFinanceFundBo.getRepayChargeOrder()); //是否预收
        //利息
        map.put("interestFee", contractFinanceFundBo.getInterestFee());
        map.put("interestDiscount", contractFinanceFundBo.getDiscount2()); //利息折扣
        map.put("interestRate", contractFinanceFundBo.getInterestFeeRate()); //利息利率
        map.put("interestReceiverName", contractFinanceFundBo.getInterestReceiverName()); //利息收款方
        map.put("interestChargeOrder", contractFinanceFundBo.getInterestChargeOrder()); //利息是否预收

        String jsonData = JSONConvert.toString(map);
        try {
            logger.info("【台账系统】[提审推台账--资金类] request -> {}, {}", contractInfoBo.getLoanNo(), jsonData);
            rabbitTemplate.convertAndSend(MqConstants.FINANCE_EXCHANGE, MessageKeyEnum.PUSHMSGTOFINANCE.getName(), jsonData);
        } catch (Exception e) {
            logger.info("【台账系统】[提审推台账--资金类] error -> {}", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--资金类]失败, " + e.getMessage() + "");
        }
    }

    /**
     * 提审推台账--担保类
     */
    public void auditGuaranteeToFinance(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        CombinationProductDetailBo productDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        ContractFinanceBo contractFinanceBo = contractInfoApi.getContractFinanceGuarantee(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceBo)) throw new BusinessException("请先保存合同，再提交！");
        ContactAuditRequest request = new ContactAuditRequest();
        request.setBusinessId(contractInfoBo.getLoanNo()); //合同编号(金融单号)
        request.setFinanceId(contractInfoBo.getProductNo()); //产品编号
        request.setProductName(productDetailBo.getCombinationName()); //产品名称
        if (Objects.nonNull(contractFinanceBo.getHouseMortgageStatus())) {
            request.setMortgageType(contractFinanceBo.getHouseMortgageStatus()); //抵押类型
        }
        request.setOrignCode(productDetailBo.getCityCode()); //区域编码
        request.setOrignName(productDetailBo.getCityName()); //区域名称
        request.setProviderId(contractFinanceBo.getBuyeridNo()); //资金方（乙方、买方）身份证号
        request.setProviderName(contractFinanceBo.getBuyerName()); //资金方姓名
        request.setBorrowName(contractFinanceBo.getSellerName()); //借款人姓名

        if (hasFinanceCharge(contractInfoBo.getProductNo())) {
            request.setStandardFee(new BigDecimal(0)); //担保服务费标准应收
            request.setContractFee(new BigDecimal(0)); //担保服务费实际应收
            request.setFirstFee(new BigDecimal(0)); //甲方支付担保服务费
            request.setSecondFee(new BigDecimal(0)); //乙方支付担保服务费
        } else {
            request.setStandardFee(contractFinanceBo.getStandardFee()); //担保服务费标准应收
            request.setContractFee(contractFinanceBo.getGuaranteeFee()); //担保服务费实际应收
            request.setFirstFee(contractFinanceBo.getSellerGuaranteeFee()); //甲方支付担保服务费
            request.setSecondFee(contractFinanceBo.getBuyerGuaranteeFee()); //乙方支付担保服务费
        }
        request.setBorrowMoney(contractFinanceBo.getUseAmount()); //借款金额
        request.setAdviserId(contractInfoBo.getCreatedId().intValue()); //金融顾问id
        request.setAdviserName(contractInfoBo.getCreatedName());

        JSONObject jsonObject;
        try {
            logger.info("【台账系统】[提审推台账--担保类] request -> {}, {} ", contractInfoBo.getLoanNo(), JSONConvert.toString(request, ContactAuditRequest.class));
            jsonObject = contactAuditApi.contactAudit(request);
            logger.info("【台账系统】[提审推台账--担保类] response -> {}, {} ", contractInfoBo.getLoanNo(), Objects.isNull(jsonObject) ? null : JSONConvert.toString(jsonObject));
        } catch (Exception e) {
            logger.info("【台账系统】[提审推台账--担保类] error -> {}", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--担保类]失败, " + e.getMessage() + "");
        }
        int code = jsonObject.optInt("code");
        String msg = jsonObject.optString("msg");
        if (0 == code) throw new BusinessException("【台账系统】[提审推台账-担保类]失败, " + msg + "");
    }

    /**
     * 提审推台账--居间类
     *
     * @param agreementBo
     */
    public void auditIntermediaryToFinance(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        CombinationProductDetailBo productDetailBo = productService.getProductDetailByNo(contractInfoBo.getProductNo());
        ContractFinanceJujianBo contractFinanceJujianBo = contractInfoApi.getContractFinanceIntermediary(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceJujianBo)) throw new BusinessException("请先保存合同，再提交！");
        Map<String, Object> map = new HashMap<>();

        ContractBo contractBo = editorService.getAgreementFieldByNo(agreementBo.getAgreementNo());
        Map<String, Object> parametersMap = JSONConvert.fromString(Map.class, contractBo.getParameters());

        //贷款合同编号
        if (Objects.nonNull(parametersMap.get("ProviderContractNo"))) {
            map.put("agreementNo", parametersMap.get("ProviderContractNo"));
        } else if (Objects.nonNull(parametersMap.get("applyContractNo"))) {
            map.put("agreementNo", parametersMap.get("applyContractNo"));
        } else if (Objects.nonNull(parametersMap.get("LoanContractContractNo"))) {
            map.put("agreementNo", parametersMap.get("LoanContractContractNo"));
        }

        map.put("contractNo", contractInfoBo.getLoanNo()); //金融单号
        map.put("transactionNo", contractInfoBo.getTransactionNo()); //交易单编号
        map.put("productNo", contractInfoBo.getProductNo()); //金融产品id
        map.put("productName", productDetailBo.getProductName()); //产品名称
        map.put("productType", ProductTypeEnum.getProductTypeIndexByName(productDetailBo.getProductUseType())); //产品类型
        map.put("adviserName", contractInfoBo.getCreatedName()); //金融顾问
        map.put("adviserId", contractInfoBo.getCreatedId()); //金融顾问系统号
        map.put("borrowName", contractFinanceJujianBo.getEntrustName()); //借款人姓名
        map.put("providerId", productDetailBo.getCooperationCode()); //资金方id
        map.put("providerName", productDetailBo.getCooperationName()); //资金方姓名
        map.put("citycode", productDetailBo.getCityCode()); //地区编码
        map.put("cityName", productDetailBo.getCityName()); //地区名称
        map.put("mortgageType", 0); //抵押类型
        map.put("repayMethod", 0); //还款方式

        map.put("borrowMoney", contractFinanceJujianBo.getBorrowAmount()); //借款金额
        map.put("signDate", contractFinanceJujianBo.getSignDate()); //合同签署日期
        map.put("loanServiceFee", contractFinanceJujianBo.getLoanFee()); //贷款服务费合同应收总费用
        map.put("loanServiceFeeStand", contractFinanceJujianBo.getLoanFeeStand()); //标准应收总费用


        map.put("chargeId", "1018"); //费用项id
        map.put("receiverName", contractFinanceJujianBo.getReceiverName()); //该费用项收款方
        map.put("chargeOrder", contractFinanceJujianBo.getChargeOrder()); //该费用项是否预收
        map.put("dicount", contractFinanceJujianBo.getDiscount()); //该费用项折扣
        map.put("rate", contractFinanceJujianBo.getRate()); //该费用项费率
        map.put("propertyAssessmentFee", contractFinanceJujianBo.getPropertyAssessFee()); //房产评估费用

        try {
            String jsonData = JSONConvert.toString(map);
            logger.info("【台账系统】[提审推台账--居间类] request -> {},{} ", contractInfoBo.getLoanNo(), jsonData);
            rabbitTemplate.convertAndSend(MqConstants.FINANCE_EXCHANGE, MessageKeyEnum.PUSHMSGTOFINACEJUJIAN.getName(), jsonData);
        } catch (Exception e) {
            logger.info("【台账系统】[提审推台账--居间类] error -> {}", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--居间类]失败, " + e.getMessage() + "");
        }
    }

    /**
     * 签约推台账
     *
     * @param agreementBo
     */
    public void signToFinance(AgreementBo agreementBo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        Map<String, Object> map = new HashMap<>();
        map.put("businessId", agreementBo.getLoanNo());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        map.put("signDate", simpleDateFormat.format(date));
        map.put("adviserName", contractInfoBo.getCreatedName());
        map.put("adviserId", contractInfoBo.getCreatedId());

        try {
            String jsonData = JSONConvert.toString(map);
            logger.info("【台账系统】[签约推台账] request -> {},{} ", agreementBo.getLoanNo(), jsonData);
            rabbitTemplate.convertAndSend(MqConstants.FINANCE_EXCHANGE, MessageKeyEnum.PUSHTIMEMSGTOFINANCE.getName(), jsonData);
        } catch (Exception e) {
            logger.info("【台账系统】[签约推台账] error -> {}", agreementBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[签约推台账]失败, " + e.getMessage() + "");
        }
    }

    /**
     * 提审推送台账：10-->居间担保
     */
    public void auditToFinanceGuaranteeNew(String agreementNo, ContractInfoBo contractInfoBo, List<FeeItemBo> feeItemBoList, CombinationProductDetailBo productInfo, Map<String, Object> fieldValueMap) {
        ContractFinanceBo contractFinanceBo = contractInfoApi.getContractFinanceGuarantee(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceBo)) throw new BusinessException("请先保存合同，再提交！");
        UnStandardContractAuditRequest unStandardContractAuditRequest = new UnStandardContractAuditRequest();
        unStandardContractAuditRequest.setBusinessId(contractInfoBo.getLoanNo()); //金融单号
        unStandardContractAuditRequest.setContractNo(agreementNo); //协议号
        unStandardContractAuditRequest.setTransactionNo(contractInfoBo.getTransactionNo()); //交易单编号

        unStandardContractAuditRequest.setProductNo(productInfo.getCombinationCode()); //金融产品id
        unStandardContractAuditRequest.setProductName(productInfo.getCombinationName()); //产品名称
        unStandardContractAuditRequest.setProductType(ProductTypeEnum.getNewProductTypeIdByCode(productInfo.getProductUseType()));//产品类型

        unStandardContractAuditRequest.setAdviserName(contractInfoBo.getCreatedName()); //金融顾问
        if (contractInfoBo.getCreatedId() != null) {
            unStandardContractAuditRequest.setAdviserId(contractInfoBo.getCreatedId().intValue()); //金融顾问系统号
        }
        unStandardContractAuditRequest.setBorrowName(contractFinanceBo.getSellerName()); //借款人姓名
        unStandardContractAuditRequest.setProviderId(productInfo.getCooperationCode()); //资金方id
        unStandardContractAuditRequest.setProviderName(productInfo.getCooperationName()); //资金方姓名
        unStandardContractAuditRequest.setCityCode(productInfo.getCityCode()); //地区编码
        unStandardContractAuditRequest.setCityName(productInfo.getCityName()); //地区名称
        unStandardContractAuditRequest.setMortgageType(0); //抵押类型
        unStandardContractAuditRequest.setRepayMethod(0); //还款方式

        unStandardContractAuditRequest.setBorrowMoney(contractFinanceBo.getUseAmount()); //借款金额
        unStandardContractAuditRequest.setSignDate(contractFinanceBo.getSignDate()); //合同签署日期
        //贷款合同编号
        if (Objects.nonNull(fieldValueMap.get("ProviderContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("ProviderContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("applyContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("applyContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("LoanContractContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("LoanContractContractNo")));
        }
        //借款期限
        unStandardContractAuditRequest.setDeadLine(contractFinanceBo.getApplyDeadline());
        // 借款期限单位、：担保类不存在
        //应收
        List<UnStandardContractRepaymentDto> receList = new ArrayList<>();
        this.assemReciveList(feeItemBoList, fieldValueMap, receList, contractInfoBo.getProductType());
        unStandardContractAuditRequest.setReceList(receList);
        //合同期数
        unStandardContractAuditRequest.setContractPeriods(receList.size());
        JSONObject result;
        try {
            logger.info("【台账系统】[提审推台账--居间担保类] request -> {}, {}", contractInfoBo.getLoanNo(), JSONConvert.toString(unStandardContractAuditRequest));
            result = unStandardContactAuditApi.contactAudit(unStandardContractAuditRequest);
            logger.info("【台账系统】[提审推台账--居间担保类] response -> {}, {}", contractInfoBo.getLoanNo(), Objects.isNull(result) ? null : JSONConvert.toString(result));
        } catch (Exception e) {
            logger.error("【台账系统】[提审推台账--居间担保类] error -> {} ", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--居间担保类] 失败, " + e.getMessage());
        }
        if (result == null || Integer.valueOf(0).equals(result.optInt("code")) || Integer.valueOf(2).equals(result.optInt("code"))) {
            throw new BusinessException("【台账系统】[提审推台账--居间担保类] 失败, " + (Objects.isNull(result) ? "null" : result.optString("msg")));
        }
    }

    /**
     * 提审推送台账:11居间资金
     */
    public void auditToFinaceFundNew(String agreementNo, ContractInfoBo contractInfoBo, List<FeeItemBo> feeItemBoList, CombinationProductDetailBo productInfo, Map<String, Object> fieldValueMap) {
        ContractFinanceFundBo contractFinanceFundBo = contractInfoApi.getContractFinanceFund(contractInfoBo.getLoanNo());
        if (Objects.isNull(contractFinanceFundBo)) throw new BusinessException("请先保存合同，再提交！");
        UnStandardContractAuditRequest unStandardContractAuditRequest = new UnStandardContractAuditRequest();
        unStandardContractAuditRequest.setBusinessId(contractInfoBo.getLoanNo()); //金融单号
        unStandardContractAuditRequest.setContractNo(agreementNo); //协议号
        unStandardContractAuditRequest.setTransactionNo(contractInfoBo.getTransactionNo()); //交易单编号

        unStandardContractAuditRequest.setProductNo(productInfo.getCombinationCode()); //金融产品id
        unStandardContractAuditRequest.setProductName(productInfo.getCombinationName()); //产品名称
        unStandardContractAuditRequest.setProductType(ProductTypeEnum.getNewProductTypeIdByCode(productInfo.getProductUseType()));//产品类型

        unStandardContractAuditRequest.setAdviserName(contractInfoBo.getCreatedName()); //金融顾问
        if (contractInfoBo.getCreatedId() != null) {
            unStandardContractAuditRequest.setAdviserId(contractInfoBo.getCreatedId().intValue()); //金融顾问系统号
        }
        unStandardContractAuditRequest.setBorrowName(contractFinanceFundBo.getBorrowerName()); //借款人姓名
        unStandardContractAuditRequest.setProviderId(productInfo.getCooperationCode());  //资金方id
        unStandardContractAuditRequest.setProviderName(productInfo.getCooperationName()); //资金方姓名
        unStandardContractAuditRequest.setCityCode(productInfo.getCityCode()); //地区编码
        unStandardContractAuditRequest.setCityName(productInfo.getCityName()); //地区名称
        unStandardContractAuditRequest.setRepayMethod(0); //还款方式
        //抵押类型
        if (Objects.nonNull(fieldValueMap.get("CreditorNumber"))) {
            unStandardContractAuditRequest.setMortgageType(Integer.valueOf(fieldValueMap.get("CreditorNumber").toString()));
        } else {
            unStandardContractAuditRequest.setMortgageType(0);
        }
        //贷款合同编号
        if (Objects.nonNull(fieldValueMap.get("ProviderContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("ProviderContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("applyContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("applyContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("LoanContractContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("LoanContractContractNo")));
        }

        unStandardContractAuditRequest.setBorrowMoney(contractFinanceFundBo.getBorrowingAmount()); //借款金额
        unStandardContractAuditRequest.setSignDate(contractFinanceFundBo.getSignDate()); //合同签署日期
        //借款期限
        unStandardContractAuditRequest.setDeadLine(contractFinanceFundBo.getBorrowingTime());
        //借款期限单位、：资金类不存在
        //应收
        List<UnStandardContractRepaymentDto> receList = new ArrayList<>();
        this.assemReciveList(feeItemBoList, fieldValueMap, receList, contractInfoBo.getProductType());
        unStandardContractAuditRequest.setReceList(receList);
        //合同期数
        unStandardContractAuditRequest.setContractPeriods(receList.size());
        JSONObject result;
        try {
            logger.info("【台账系统】[提审推台账--居间资金类] request -> {}, {} ", contractInfoBo.getLoanNo(), JSONConvert.toString(unStandardContractAuditRequest));
            result = unStandardContactAuditApi.contactAudit(unStandardContractAuditRequest);
            logger.info("【台账系统】[提审推台账--居间资金类] response -> {}, {} ", contractInfoBo.getLoanNo(), Objects.isNull(result) ? null : JSONConvert.toString(result));
        } catch (Exception e) {
            logger.info("【台账系统】[提审推台账--居间资金类] error -> {}", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--居间资金类] 失败, " + e.getMessage());
        }
        if (result == null || Integer.valueOf(0).equals(result.optInt("code")) || Integer.valueOf(2).equals(result.optInt("code"))) {
            throw new BusinessException("【台账系统】[提审推台账--居间资金类] 失败, " + (Objects.isNull(result) ? "null" : result.optString("msg")));
        }
    }


    /**
     * 提审-服务类
     */
    public void auditToFinaceServiceNew(String agreementNo, ContractInfoBo contractInfoBo, List<FeeItemBo> feeItemBoList,
                                        CombinationProductDetailBo productInfo, Map<String, Object> fieldValueMap) {
        UnStandardContractAuditRequest unStandardContractAuditRequest = new UnStandardContractAuditRequest();
        unStandardContractAuditRequest.setBusinessId(contractInfoBo.getLoanNo()); //金融单号
        unStandardContractAuditRequest.setTransactionNo(contractInfoBo.getTransactionNo()); //交易单编号
        unStandardContractAuditRequest.setContractNo(agreementNo); //协议号
        unStandardContractAuditRequest.setProductNo(productInfo.getCombinationCode()); //金融产品id
        unStandardContractAuditRequest.setProductName(productInfo.getCombinationName()); //产品名称
        unStandardContractAuditRequest.setProductType(ProductTypeEnum.getNewProductTypeIdByCode(productInfo.getProductUseType()));//产品类型

        unStandardContractAuditRequest.setAdviserName(contractInfoBo.getCreatedName()); //金融顾问
        if (contractInfoBo.getCreatedId() != null) {
            unStandardContractAuditRequest.setAdviserId(contractInfoBo.getCreatedId().intValue()); //金融顾问系统号
        }
        String partyACustomerName = String.valueOf(fieldValueMap.get("PartyACustomerName"));
        String partyAEntrustedParty = String.valueOf(fieldValueMap.get("PartyAEntrustedParty"));
        unStandardContractAuditRequest.setBorrowName(StringUtils.hasText(partyACustomerName) ? partyACustomerName : partyAEntrustedParty);//借款人姓名
        unStandardContractAuditRequest.setProviderId(productInfo.getCooperationCode()); //资金方id
        unStandardContractAuditRequest.setProviderName(productInfo.getCooperationName()); //资金方姓名
        unStandardContractAuditRequest.setCityCode(productInfo.getCityCode()); //地区编码
        unStandardContractAuditRequest.setCityName(productInfo.getCityName()); //地区名称
        unStandardContractAuditRequest.setMortgageType(0); //抵押类型
        unStandardContractAuditRequest.setRepayMethod(0); //还款方式


        String borrowingAmount = Objects.nonNull(fieldValueMap.get("BorrowingAmount")) ? String.valueOf(fieldValueMap.get("BorrowingAmount")) : null;
        String juJianBorrowingAmount = Objects.nonNull(fieldValueMap.get("JuJianBorrowingAmount")) ? String.valueOf(fieldValueMap.get("JuJianBorrowingAmount")) : null;
        String zHXT01SINGLEloanAmt = Objects.nonNull(fieldValueMap.get("ZHXT01SINGLEloanAmt")) ? String.valueOf(fieldValueMap.get("ZHXT01SINGLEloanAmt")) : null;
        String borrowMoney = StringUtils.hasText(borrowingAmount) ? borrowingAmount : StringUtils.hasText(juJianBorrowingAmount) ? juJianBorrowingAmount : zHXT01SINGLEloanAmt;
        //借款金额
        if (StringUtils.hasText(borrowMoney)) {
            unStandardContractAuditRequest.setBorrowMoney(new BigDecimal(borrowMoney.trim()));
        }
        // 合同签署日期
        String signTime = this.getSignTime(fieldValueMap);
        if (StringUtils.hasText(signTime)) {
            unStandardContractAuditRequest.setSignDate(signTime);
        }
        //贷款合同编号
        if (Objects.nonNull(fieldValueMap.get("ProviderContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("ProviderContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("applyContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("applyContractNo")));
        } else if (Objects.nonNull(fieldValueMap.get("LoanContractContractNo"))) {
            unStandardContractAuditRequest.setContractNo(String.valueOf(fieldValueMap.get("LoanContractContractNo")));
        }
        //借款期限
        String borrowingTime = this.getBrorrowTime(fieldValueMap);
        if (StringUtils.hasText(borrowingTime)) {
            unStandardContractAuditRequest.setDeadLine(Integer.valueOf(borrowingTime.trim()));
        }
        //借款期限
        //应收
        List<UnStandardContractRepaymentDto> receList = new ArrayList<>();
        this.assemReciveList(feeItemBoList, fieldValueMap, receList, contractInfoBo.getProductType());
        unStandardContractAuditRequest.setReceList(receList);
        //合同期数
        unStandardContractAuditRequest.setContractPeriods(receList.size());
        JSONObject result;
        try {
            logger.info("【台账系统】[提审推台账--服务类] request -> {}, {} ", contractInfoBo.getLoanNo(), JSONConvert.toString(unStandardContractAuditRequest));
            result = unStandardContactAuditApi.contactAudit(unStandardContractAuditRequest);
            logger.info("【台账系统】[提审推台账--服务类] response -> {}, {} ", contractInfoBo.getLoanNo(), Objects.isNull(result) ? null : JSONConvert.toString(result));
        } catch (Exception e) {
            logger.info("【台账系统】[提审推台账--服务类] error -> {} ", contractInfoBo.getLoanNo(), e);
            throw new BusinessException("【台账系统】[提审推台账--服务类] 失败, " + e.getMessage());
        }
        if (result == null || Integer.valueOf(0).equals(result.optInt("code")) || Integer.valueOf(2).equals(result.optInt("code"))) {
            throw new BusinessException("【台账系统】[提审推台账--服务类] 失败, " + (Objects.isNull(result) ? "null" : result.optString("msg")));
        }
    }


    /**
     * 合同签署日期
     */
    private String getSignTime(Map<String, Object> fieldValueMap) {
        String signatureDate = Objects.nonNull(fieldValueMap.get("SignatureDate")) ? String.valueOf(fieldValueMap.get("SignatureDate")) : null;
        if (StringUtils.hasText(signatureDate)) return signatureDate;
        String signDate = Objects.nonNull(fieldValueMap.get("SignDate")) ? String.valueOf(fieldValueMap.get("SignDate")) : null;
        if (StringUtils.hasText(signDate)) return signDate;
        String daiKuanSignatureDate = Objects.nonNull(fieldValueMap.get("DaiKuanSignatureDate")) ? String.valueOf(fieldValueMap.get("DaiKuanSignatureDate")) : null;
        if (StringUtils.hasText(daiKuanSignatureDate)) return daiKuanSignatureDate;
        return daiKuanSignatureDate;
    }

    /**
     * 借款期限
     */
    private String getBrorrowTime(Map<String, Object> fieldValueMap) {
        String borrowingTime = Objects.nonNull(fieldValueMap.get("BorrowingTime")) ? String.valueOf(fieldValueMap.get("BorrowingTime")) : null;
        if (StringUtils.hasText(borrowingTime)) return borrowingTime;
        String zHXT01SINGLEloanTerm = Objects.nonNull(fieldValueMap.get("ZHXT01SINGLEloanTerm")) ? String.valueOf(fieldValueMap.get("ZHXT01SINGLEloanTerm")) : null;
        if (StringUtils.hasText(zHXT01SINGLEloanTerm)) return zHXT01SINGLEloanTerm;
        String periodOfLoanTime1 = Objects.nonNull(fieldValueMap.get("PeriodOfLoanTime1")) ? String.valueOf(fieldValueMap.get("PeriodOfLoanTime1")) : null;
        if (StringUtils.hasText(periodOfLoanTime1)) return periodOfLoanTime1;
        String periodOfLoanTime2 = Objects.nonNull(fieldValueMap.get("PeriodOfLoanTime2")) ? String.valueOf(fieldValueMap.get("PeriodOfLoanTime2")) : null;
        if (StringUtils.hasText(periodOfLoanTime2)) return periodOfLoanTime2;
        String guaranteeTime = Objects.nonNull(fieldValueMap.get("GuaranteeTime")) ? String.valueOf(fieldValueMap.get("GuaranteeTime")) : null;
        if (StringUtils.hasText(guaranteeTime)) return guaranteeTime;
        return guaranteeTime;
    }

    /**
     * 应收构建
     */
    private Map<String, String> getFieldValueByName(Map<String, Object> fieldValueMap, String productType) {
        if (ProductTypeEnum.FUND.getCode().equals(productType)) {
            return fundMap(fieldValueMap);
        } else if (ProductTypeEnum.GUARANTEE.getCode().equals(productType)) {
            return guaranteeMap(fieldValueMap);
        } else if (ProductTypeEnum.SERVICE.getCode().equals(productType)) {
            return serviceMap(fieldValueMap);
        }
        return new HashMap<>();
    }

    //资金费用体
    private Map<String, String> fundMap(Map<String, Object> fieldValueMap) {
        Map<String, String> resultMap = new HashMap<>();
        if (Objects.nonNull(fieldValueMap.get("GuaranteeServiceFee"))) {
            resultMap.put("担保服务费", String.valueOf(fieldValueMap.get("GuaranteeServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("ServiceFeeTotal"))) {
            resultMap.put("管理服务费", String.valueOf(fieldValueMap.get("ServiceFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("RepaymentFeeTotal"))) {
            resultMap.put("还款保证金", String.valueOf(fieldValueMap.get("RepaymentFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("LoanServiceFee"))) {
            resultMap.put("贷款服务费", String.valueOf(fieldValueMap.get("LoanServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("Interest"))) {
            resultMap.put("利息", String.valueOf(fieldValueMap.get("Interest")));
        }
        Object amount = Objects.nonNull(fieldValueMap.get("BorrowingAmount")) ? fieldValueMap.get("BorrowingAmount") : Objects.nonNull(fieldValueMap.get("ZHXT01SINGLEloanAmt")) ? fieldValueMap.get("ZHXT01SINGLEloanAmt") : null;
        if (Objects.nonNull(amount)) {
            resultMap.put("本金", String.valueOf(amount));
        }
        return resultMap;
    }

    //担保费用体
    private Map<String, String> guaranteeMap(Map<String, Object> fieldValueMap) {
        Map<String, String> resultMap = new HashMap<>();
        if (Objects.nonNull(fieldValueMap.get("GuaranteeServiceFee"))) {
            resultMap.put("担保服务费", String.valueOf(fieldValueMap.get("GuaranteeServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("ServiceFeeTotal"))) {
            resultMap.put("管理服务费", String.valueOf(fieldValueMap.get("ServiceFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("RepaymentFeeTotal"))) {
            resultMap.put("还款保证金", String.valueOf(fieldValueMap.get("RepaymentFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("LoanServiceFee"))) {
            resultMap.put("贷款服务费", String.valueOf(fieldValueMap.get("LoanServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("Interest"))) {
            resultMap.put("利息", String.valueOf(fieldValueMap.get("Interest")));
        }
        //本金
        BigDecimal amountTotal = new BigDecimal(0);
        if (Objects.nonNull(fieldValueMap.get("UseAmout"))) {
            String amount1 = String.valueOf(fieldValueMap.get("UseAmout")).split(",")[0];
            amountTotal = amountTotal.add(new BigDecimal(amount1.trim()));
        }
        if (Objects.nonNull(fieldValueMap.get("UseAmout2"))) {
            String amount2 = String.valueOf(fieldValueMap.get("UseAmout2")).split(",")[0];
            amountTotal = amountTotal.add(new BigDecimal(amount2.trim()));
        }
        if (amountTotal.compareTo(BigDecimal.ZERO) == 1) {
            resultMap.put("本金", String.valueOf(amountTotal));
        }
        return resultMap;
    }

    //服务费用体
    private Map<String, String> serviceMap(Map<String, Object> fieldValueMap) {
        Map<String, String> resultMap = new HashMap<>();
        if (Objects.nonNull(fieldValueMap.get("GuaranteeServiceFee"))) {
            resultMap.put("担保服务费", String.valueOf(fieldValueMap.get("GuaranteeServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("ServiceFeeTotal"))) {
            resultMap.put("管理服务费", String.valueOf(fieldValueMap.get("ServiceFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("RepaymentFeeTotal"))) {
            resultMap.put("还款保证金", String.valueOf(fieldValueMap.get("RepaymentFeeTotal")));
        }
        if (Objects.nonNull(fieldValueMap.get("LoanServiceFee"))) {
            resultMap.put("贷款服务费", String.valueOf(fieldValueMap.get("LoanServiceFee")));
        }
        if (Objects.nonNull(fieldValueMap.get("Interest"))) {
            resultMap.put("利息", String.valueOf(fieldValueMap.get("Interest")));
        }
        if (Objects.nonNull(fieldValueMap.get("BorrowingAmount"))) {
            resultMap.put("本金", String.valueOf(fieldValueMap.get("BorrowingAmount")));
        }
        return resultMap;
    }

    private void assemReciveList(List<FeeItemBo> feeItemBoList, Map<String, Object> fieldValueMap, List<UnStandardContractRepaymentDto> receList, String productType) {
        int periodNum = 1;
        logger.info("assemReciveList request: {}, {}", fieldValueMap.toString(), productType);
        Map<String, String> resultMap = getFieldValueByName(fieldValueMap, productType);
        logger.info("assemReciveList response: {}", resultMap.toString());
        for (FeeItemBo feeItemBo : feeItemBoList) {
            UnStandardContractRepaymentDto unStandardContractRepaymentDto = new UnStandardContractRepaymentDto();
            unStandardContractRepaymentDto.setFeeModel(feeItemBo.getFeeItemName());
            String receivableFee = resultMap.get(feeItemBo.getFeeItemName());
            if (Objects.isNull(receivableFee) || "null".equals(receivableFee)) continue;
            unStandardContractRepaymentDto.setReceivableFee(new BigDecimal(receivableFee.trim()));
            unStandardContractRepaymentDto.setPeriods(periodNum);
            unStandardContractRepaymentDto.setCollectionName(feeItemBo.getReceiveSide());
            unStandardContractRepaymentDto.setIsPrepay(this.getIsPrepay(feeItemBo.getAdvanceReceive()));
            receList.add(unStandardContractRepaymentDto);
            periodNum++;
        }
    }

    private Integer getIsPrepay(String advanceReceive) {
        if (!StringUtils.hasText(advanceReceive)) return null;
        return "Y".equals(advanceReceive) ? 1 : 2;
    }

}
