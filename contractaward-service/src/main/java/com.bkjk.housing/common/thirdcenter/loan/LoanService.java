package com.bkjk.housing.common.thirdcenter.loan;

import com.bkjk.housing.common.enums.BusinessTypeEnum;
import com.bkjk.housing.common.enums.ProductTypeEnum;
import com.bkjk.housing.common.enums.SupplementAgreementEnum;
import com.bkjk.housing.common.enums.TerminationAgreementEnum;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceFundBo;
import com.bkjk.housing.contractaward.agreement.domain.ContractFinanceJujianBo;
import com.bkjk.housing.contractaward.contract.api.ContractInfoApi;
import com.bkjk.housing.loan.externalsys.domain.CapitalSerialNumberBo;
import com.bkjk.housing.loan.externalsys.spi.CapitalSeriaNumberSpi;
import com.bkjk.housing.loan.loans.domain.ContractInvalidBo;
import com.bkjk.housing.loan.loans.domain.ContractSignBo;
import com.bkjk.housing.loan.loans.domain.ContractSupplementBo;
import com.bkjk.housing.loan.loans.domain.ContractTerminateBo;
import com.bkjk.housing.loan.loans.spi.LoanInformationSpi;
import com.bkjk.housing.loan.loans.spi.SignInformationSpi;
import com.bkjk.platform.contract.contract.api.ContractApi;
import com.bkjk.platform.contract.contract.domain.ContractBo;
import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
public class LoanService {
    private final Logger logger = LoggerFactory.getLogger(LoanService.class);
    @Inject
    private SignInformationSpi signInformationSpi;
    @Inject
    private ContractApi contractApi;

    @Inject
    private ContractInfoApi contractInfoApi;

    @Inject
    private CapitalSeriaNumberSpi capitalSeriaNumberSpi;

    @Inject
    private LoanInformationSpi loanInformationSpi;

    /**
     * 合同设置无效
     *
     * @param loanNo
     * @return
     */
    public boolean contractInvalid(String loanNo) {
        ContractInvalidBo contractInvalidBo = new ContractInvalidBo();
        contractInvalidBo.setLoanNo(loanNo);
        logger.info("【进件】[合同设置无效] request -> {} ", loanNo);
        Boolean flag = signInformationSpi.contractInvalid(contractInvalidBo);
        logger.info("【进件】[合同设置无效] response -> {},{} ", loanNo, JSONConvert.toString(flag));
        if (!flag) throw new BusinessException("【进件】推送合同无效至金融单失败");
        return true;
    }


    /**
     * 推送主协议
     *
     * @return
     */
    public boolean contractmain(String loanNo, String productType) {
        ContractSignBo contractSignBo = new ContractSignBo();
        contractSignBo.setLoanNo(loanNo);
        try {
            Date signTime = this.getSignDate(loanNo, productType);
            contractSignBo.setSignTime(signTime);
            logger.info("【进件】[推送合同签约时间] request -> {},{} ", loanNo, JSONConvert.toString(contractSignBo));
            Boolean flag = signInformationSpi.contractSign(contractSignBo);
            logger.info("【进件】[推送合同签约时间] response -> {},{} ", loanNo, JSONConvert.toString(flag));
            if (!flag) {
                throw new BusinessException("【进件】推送合同签约时间失败");
            }
        } catch (Exception e) {
            logger.error("【进件】[推送合同签约时间] error -> {},{} ", loanNo, e.getMessage());
            throw new BusinessException("【进件】推送合同签约时间失败 {}", e.getMessage());
        }
        return true;
    }

    private Date getSignDate(String loanNo, String productType) throws Exception {
        String signDate = null;
        if (ProductTypeEnum.FUND.getCode().equals(productType)) {
            ContractFinanceFundBo contractFinanceFundBo = contractInfoApi.getContractFinanceFund(loanNo);
            signDate = contractFinanceFundBo.getSignDate();
        } else if (ProductTypeEnum.GUARANTEE.getCode().equals(productType)) {
            ContractFinanceBo contractFinanceBo = contractInfoApi.getContractFinanceGuarantee(loanNo);
            signDate = contractFinanceBo.getSignDate();
        } else if (ProductTypeEnum.INTERMEDIARY.getCode().equals(productType)) {
            ContractFinanceJujianBo contractFinanceJujianBo = contractInfoApi.getContractFinanceIntermediary(loanNo);
            signDate = contractFinanceJujianBo.getSignDate();
        }
        Date formatSignDate = new Date();
        if (Objects.nonNull(signDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (signDate.indexOf("年") == -1) {
                formatSignDate = sdf.parse(signDate);
            } else {
                String yeah = signDate.substring(0, 4); //取年
                String yue = signDate.substring(signDate.indexOf("年") + 1, signDate.indexOf("月")); //取月
                String ri = signDate.substring(signDate.indexOf("月") + 1, signDate.indexOf("日")); //取日
                String dateString = yeah + "-" + yue + "-" + ri;
                formatSignDate = sdf.parse(dateString);
            }
        }
        return formatSignDate;
    }


    /**
     * 推送补充协议
     *
     * @param loanNo
     * @param agreementNo
     * @return
     */
    public boolean contractSupplement(String loanNo, String agreementNo, SupplementAgreementEnum
            supplementAgreementEnum) {
        ContractBo contractBo = contractApi.getByContractNo(agreementNo);
        if (!StringUtils.hasText(contractBo.getParameters())) {
            throw new BusinessException("合同信息数据为null");
        }
        Map<String, String> map = JSONConvert.fromString(Map.class, contractBo.getParameters());

        ContractSupplementBo contractSupplementBo = new ContractSupplementBo();
        contractSupplementBo.setLoanNo(loanNo);
        contractSupplementBo.setState(supplementAgreementEnum.getCode());

        if (StringUtils.hasText(map.get("BorrowBankBranch")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceBankBranch"))) {
            contractSupplementBo.setBranchBank(StringUtils.hasText(map.get("BorrowBankBranch")) ? map.get("BorrowBankBranch") : map.get("ZHXT01SINGLEreceBankBranch"));
        }
        if (StringUtils.hasText(map.get("BorrowBankAccountName")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceAccName"))) {
            contractSupplementBo.setBankAccountName(StringUtils.hasText(map.get("BorrowBankAccountName")) ? map.get("BorrowBankAccountName") : map.get("ZHXT01SINGLEreceAccName"));
        }

        if (StringUtils.hasText(map.get("BorrowBankAccount")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceAccNo"))) {
            contractSupplementBo.setBankAccountNumber(StringUtils.hasText(map.get("BorrowBankAccount")) ? map.get("BorrowBankAccount") : map.get("ZHXT01SINGLEreceAccNo"));
        }

        if (StringUtils.hasText(map.get("BorrowBank")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceBankName"))) {
            contractSupplementBo.setOpeningBank(StringUtils.hasText(map.get("BorrowBank")) ? map.get("BorrowBank") : map.get("ZHXT01SINGLEreceBankName"));
        }
        if (StringUtils.hasText(map.get("MortageHouseLocation")) || StringUtils.hasText(map.get("SellHouseAddressChange"))) {
            contractSupplementBo.setPropertyAddress(StringUtils.hasText(map.get("MortageHouseLocation")) ? map.get("MortageHouseLocation") : map.get("SellHouseAddressChange"));
        }
        logger.info("【进件】[推送补充协议] request -> {},{} ", loanNo, JSONConvert.toString(contractSupplementBo));
        Boolean flag = signInformationSpi.contractSupplement(contractSupplementBo);
        logger.info("【进件】[推送补充协议] response -> {},{} ", loanNo, JSONConvert.toString(flag));
        if (!flag) throw new BusinessException("【进件】推送补充协议至金融单失败");
        return true;
    }

    /**
     * 推送解约协议
     *
     * @param loanNo
     * @return
     */
    public boolean contractTerminate(String loanNo, TerminationAgreementEnum terminationAgreementEnum) {
        ContractTerminateBo contractTerminateBo = new ContractTerminateBo();
        contractTerminateBo.setLoanNo(loanNo);
        contractTerminateBo.setState(terminationAgreementEnum.getCode());
        logger.info("【进件】[推送解约协议] request -> {},{} ", loanNo, JSONConvert.toString(contractTerminateBo));
        Boolean flag = signInformationSpi.contractTerminate(contractTerminateBo);
        logger.info("【进件】[推送解约协议] response -> {},{} ", loanNo, JSONConvert.toString(flag));
        if (!flag) throw new BusinessException("【进件】推送解约协议至金融单失败");
        return true;
    }

    /**
     * 获取进件字段
     *
     * @param loanNo
     * @return
     */
    public Map<String, String> getLoanFields(String loanNo) {
        logger.info("【进件】[获取进件字段] request -> {} ", loanNo);
        Map<String, String> loanMap = loanInformationSpi.getLoanInformationsByLoanNo(loanNo);
        logger.info("【进件】[获取进件字段] response -> {},{} ", loanNo, Objects.isNull(loanMap) ? null : JSONConvert.toString(loanMap, Map.class));
        return loanMap;
    }

    /**
     * 推送进件资金方信息
     *
     * @param agreementBo
     * @param checkedChannelCode
     * @param fundLoanNo
     * @return
     */
    public CapitalSerialNumberBo saveCapitalSerialNumber(AgreementBo agreementBo, String checkedChannelCode, String
            fundLoanNo) {
        CapitalSerialNumberBo capitalSerialNumberBo = new CapitalSerialNumberBo();
        capitalSerialNumberBo.setLoanNo(agreementBo.getLoanNo());
        capitalSerialNumberBo.setChannelCode(checkedChannelCode);
        capitalSerialNumberBo.setCapitalContractNo(agreementBo.getAgreementNo());
        capitalSerialNumberBo.setBorrowSerialNumber(fundLoanNo);
        logger.info("【进件】[推送进件资金方信息] request -> {},{} ", agreementBo.getLoanNo(), JSONConvert.toString(capitalSerialNumberBo));
        CapitalSerialNumberBo response = capitalSeriaNumberSpi.saveCapitalSerialNumber(capitalSerialNumberBo);
        logger.info("【进件】[推送进件资金方信息] response -> {},{} ", agreementBo.getLoanNo(), Objects.isNull(response) ? null : JSONConvert.toString(response));
        return response;
    }


}
