package com.bkjk.housing.common.thirdcenter.capitalplatform;

import com.bkjk.housing.classic.combination.api.domain.CombinationProductDetailBo;
import com.bkjk.housing.common.thirdcenter.capitalplatform.domain.FundContractDocReqest;
import com.bkjk.housing.common.thirdcenter.capitalplatform.domain.FundInfoRequest;
import com.bkjk.housing.common.thirdcenter.capitalplatform.domain.FundInfoResponseBo;
import com.bkjk.housing.common.util.ThirdPartyApiUtil;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.contract.domain.ContractInfoBo;
import com.bkjk.platform.contract.contract.api.ContractApi;
import com.bkjk.platform.contract.contract.domain.ContractBo;
import com.bkjk.platform.contract.template.entity.TemplateType;
import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.logging.LoggerFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class CapitalPlatformService {
    private final Logger logger = LoggerFactory.getLogger(CapitalPlatformService.class);

    @Value("${com.bkjk.housing.zjpt.url}")
    private String zjptUrl;

    @Value("${com.bkjk.housing.sign.url}")
    private String signNotifyUrl;

    @Inject
    private ThirdPartyApiUtil thirdPartyApiUtil;

    @Inject
    private ContractApi contractApi;

    public FundInfoResponseBo getFundInfo(AgreementBo agreementBo, CombinationProductDetailBo productDetailBo, String fundLoanNo) {
        ContractInfoBo contractInfoBo = agreementBo.getContractInfoBo();
        ContractBo contractBo = contractApi.getByContractNo(agreementBo.getAgreementNo());
        if (!StringUtils.hasText(contractBo.getParameters())) {
            throw new BusinessException("合同信息数据为null");
        }
        Map<String, String> map = JSONConvert.fromString(Map.class, contractBo.getParameters());
        String getFundInfoUrl = zjptUrl + "/fund/api/house/getFundInfo";

        //定义数组
        String borrowerName[] = {}; //借款人

        String borroweridNo[] = {}; //借款人证件号码

        String borrowerIdType[] = {}; //借款人证件类型

        String borrowerPhone[] = {}; //借款人联系方式

        String borrowerLegalPeople[] = {}; //法定代表人

        String borrowerLegalPeopleIdType[] = {}; //法人证件类型

        String borrowerLegalPeopleIdNumber[] = {}; //法人证件号码

        String borrowerLegalPeoplePhone[] = {}; //法人手机号码

        String signCity = contractInfoBo.getCityCode(); //进件城市

        String borrowerType = "";  //借款人类型

        String borrowingAmount = map.get("BorrowingAmount");  //借款金额

        String borrowingTime = map.get("BorrowingTime"); //借款期限


        if (StringUtils.hasText(map.get("BorrowerIdType"))) { //借款人证件类型
            borrowerIdType = map.get("BorrowerIdType").split(",");
        }

        if (StringUtils.hasText(map.get("BorrowerName"))) { //借款人
            borrowerName = map.get("BorrowerName").split(",");
        }

        if (StringUtils.hasText(map.get("BorrowerPhone"))) { //借款人联系方式
            borrowerPhone = map.get("BorrowerPhone").split(",");
        }

        if (StringUtils.hasText(map.get("BorrowerIdNumber"))) { //借款人证件号码
            borroweridNo = map.get("BorrowerIdNumber").split(",");
        }

        if (StringUtils.hasText(map.get("PartyALegalPeople"))) { //法人姓名
            borrowerLegalPeople = map.get("PartyALegalPeople").split(",");
        }
        if (StringUtils.hasText(map.get("ZHXT01SINGLEownerLegal"))) { //中航信托01权属人法人代表
            borrowerLegalPeople = map.get("ZHXT01SINGLEownerLegal").split(",");
        }
        if (StringUtils.hasText(map.get("ZHXT01jointlistjointLegal"))) { //中航信托01共有人法人代表
            borrowerLegalPeople = map.get("ZHXT01jointlistjointLegal").split(",");
        }
        if (StringUtils.hasText(map.get("BorrowerLegalPeopleIdType"))) { //法人证件类型
            borrowerLegalPeopleIdType = map.get("BorrowerLegalPeopleIdType").split(",");
        }
        if (StringUtils.hasText(map.get("BorrowerLegalPeopleIdNumber"))) { //法人证件号码
            borrowerLegalPeopleIdNumber = map.get("BorrowerLegalPeopleIdNumber").split(",");
        }
        if (StringUtils.hasText(map.get("BorrowerLegalPeoplePhone"))) { //法人手机号码
            borrowerLegalPeoplePhone = map.get("BorrowerLegalPeoplePhone").split(",");
        }
        if (StringUtils.hasText(map.get("SignCity"))) {
            signCity = map.get("SignCity");
        }

        FundInfoRequest fundInfoRequest = new FundInfoRequest();
        List<FundInfoRequest.BorrowerInfo> borrowerInfoList = Lists.newArrayList();
        List<FundInfoRequest.LoanOrgInfo> loanOrgInfoList = Lists.newArrayList();

        for (int i = 0; i < borrowerName.length; i++) {
            //借款人为个人
            FundInfoRequest.BorrowerInfo borrowerInfo = new FundInfoRequest.BorrowerInfo();
            //借款人为公司
            FundInfoRequest.LoanOrgInfo loanOrgInfo = new FundInfoRequest.LoanOrgInfo();
            if (borrowerIdType.length > i && borrowerIdType.length > 0 && borrowerIdType[i] != null && !borrowerIdType[i].equals("NULL")) {
                if (borrowerIdType[i].equals("4")) {
                    loanOrgInfo.setLoanOrgName(borrowerName[i]);
                    loanOrgInfo.setOrgCertiType("b");
                    loanOrgInfo.setOrgCertiCode(borroweridNo[i]);
                    loanOrgInfo.setCustomerName(borrowerLegalPeople[i]);
                    loanOrgInfo.setLegalCertType(borrowerLegalPeopleIdType[i]);
                    loanOrgInfo.setLegalCertNo(borrowerLegalPeopleIdNumber[i]);
                    loanOrgInfo.setLegalMobileNo(borrowerLegalPeoplePhone[i]);

                    borrowerType = "2";
                } else {
                    borrowerType = "1";
                    if (borrowerName.length > i && borrowerName.length > 0 && borrowerName[i] != null && !borrowerName[i].equals("NULL")) {
                        borrowerInfo.setBorrowerName(borrowerName[i]);
                    }
                    if (borroweridNo.length > i && borroweridNo.length > 0 && borroweridNo[i] != null && !borroweridNo[i].equals("NULL")) {
                        borrowerInfo.setIdCardNo(borroweridNo[i]);
                    }
                    if (borrowerPhone.length > i && borrowerPhone.length > 0 && borrowerPhone[i] != null && !borrowerPhone[i].equals("NULL")) {
                        borrowerInfo.setTelePhone(borrowerPhone[i]);
                    }

                    switch (borrowerIdType[i]) {
                        case "0":
                            borrowerInfo.setIdType("20");
                            break; //身份证
                        case "1":
                            borrowerInfo.setIdType("22");
                            break; //护照
                        case "2":
                            borrowerInfo.setIdType("26");
                            break; //台湾居民来往大陆通行证
                        case "3":
                            borrowerInfo.setIdType("25");
                            break; //港澳居民来往内地通行证

                    }
                }
            }
            borrowerInfoList.add(borrowerInfo);
            loanOrgInfoList.add(loanOrgInfo);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        final String now = simpleDateFormat.format(new Date());


        if ("15".equals(productDetailBo.getProductCode()) || "17".equals(productDetailBo.getProductCode()) || "28".equals(productDetailBo.getProductCode())) {
            fundInfoRequest.setProductId("BKSLD001");
            fundInfoRequest.setProductName("赎楼");
        } else if ("16".equals(productDetailBo.getProductCode())) {
            fundInfoRequest.setProductId("BKWKD001");
            fundInfoRequest.setProductName("尾款");
        }


        fundInfoRequest.setFundLoanNo(fundLoanNo);
        fundInfoRequest.setProductLine("L_HOUSE_FINANCE");
        fundInfoRequest.setFinanceOrderNo(agreementBo.getLoanNo());
        fundInfoRequest.setLoanApplyTime(now);
        fundInfoRequest.setLoanAmount("".equals(borrowingAmount) ? new BigDecimal(0) : new BigDecimal(borrowingAmount));
        fundInfoRequest.setLoanTerm("".equals(borrowingTime) ? 0 : Integer.parseInt(borrowingTime));
        fundInfoRequest.setTermUnit("DAY");
        fundInfoRequest.setSignRate(BigDecimal.valueOf(0.09)); //固定值
        fundInfoRequest.setRepayMode("3");
        fundInfoRequest.setLoanCity(signCity);
        fundInfoRequest.setBorrowerType(borrowerType);
        fundInfoRequest.setBorrowerNum(borrowerName.length + "");
        fundInfoRequest.setAllowPrepayment("1");
        fundInfoRequest.setBorrowerInfoList(borrowerInfoList);
        fundInfoRequest.setLoanOrgInfoList(loanOrgInfoList);

        logger.info("【资金平台】[获取资金方信息] request -> {}", JSONConvert.toString(fundInfoRequest));
        String responseText = thirdPartyApiUtil.textPost(getFundInfoUrl, JSONConvert.toString(fundInfoRequest));
        logger.info("【资金平台】[获取资金方信息] response -> {}", responseText);

        if (!StringUtils.hasText(responseText)) throw new BusinessException("调用资金平台失败！");
        FundInfoResponseBo responseBo = JSONConvert.fromString(FundInfoResponseBo.class, responseText);
        if (!"S0000".equals(responseBo.getReturnCode())) {
            throw new BusinessException("调用资金平台失败！" + responseBo.getReturnMsg());
        }
        return responseBo;
    }


    public FundInfoResponseBo getFundContractDoc(AgreementBo agreementBo, CombinationProductDetailBo productDetailBo, String fundLoanNo) {
        ContractBo contractBo = contractApi.getByContractNo(agreementBo.getAgreementNo());
        if (!StringUtils.hasText(contractBo.getParameters())) {
            throw new BusinessException("合同信息数据为null");
        }
        Map<String, String> map = JSONConvert.fromString(Map.class, contractBo.getParameters());

        FundContractDocReqest fundContractDocReqVo = new FundContractDocReqest();
        List<FundContractDocReqest.ApplyCustInfo> applyCustInfoList = Lists.newArrayList();
        List<FundContractDocReqest.Mortgagor> mortgagorList = Lists.newArrayList();

        String getFundContractDocUrl = zjptUrl + "/fund/api/house/getFundContractDoc";

        String contractNo = "";
        if (TemplateType.LOAN.getCode().equals(agreementBo.getTemplateType())) { //贷款
            fundContractDocReqVo.setCreatContType("loan"); //贷款合同

            //定义数组 借款人信息
            String borrowerName[] = {}; //借款人

            String borroweridNo[] = {}; //借款人证件号码

            String borrowerIdType[] = {}; //借款人证件类型

            String borrowerPhone[] = {}; //借款人联系方式

            String borrowerAddress[] = {}; //借款人地址

            String borrowerLegalPeople[] = {}; //法定代表人

            String providerContractNo = ""; //贷款合同编号

            String signProvince = "北京市"; //合同签署省份

            String signCity = "北京市"; //合同签署城市

            String signArea = ""; //合同签署区县

            String signatureDate = ""; //合同生成日期/合同签署日期

            String zHXT01SINGLEloanPurpose = ""; //赎楼借款用途

            String zHXT01SINGLEotherUse = ""; //借款用途为其他

            String contFileNum = ""; //贷款合同份数


            if (StringUtils.hasText(map.get("BorrowerIdType"))) { //借款人证件类型

                borrowerIdType = map.get("BorrowerIdType").split(",");

            }
            if (StringUtils.hasText(map.get("BorrowerName"))) { //借款人姓名

                borrowerName = map.get("BorrowerName").split(",");

            }
            if (StringUtils.hasText(map.get("BorrowerPhone"))) { //借款人联系方式

                borrowerPhone = map.get("BorrowerPhone").split(",");

            }
            if (StringUtils.hasText(map.get("BorrowerIdNumber"))) { //借款人证件号码

                borroweridNo = map.get("BorrowerIdNumber").split(",");

            }
            if (StringUtils.hasText(map.get("BorrowerAddress"))) { //借款人联系地址

                borrowerAddress = map.get("BorrowerAddress").split(",");

            }
            if (StringUtils.hasText(map.get("PartyALegalPeople"))) { //法人姓名

                borrowerLegalPeople = map.get("PartyALegalPeople").split(",");

            }
            if (StringUtils.hasText(map.get("ProviderContractNo"))) { //贷款合同编号

                providerContractNo = map.get("ProviderContractNo");

            }
            if (StringUtils.hasText(map.get("SignProvince"))) { //合同签署省份

                signProvince = map.get("SignProvince");

            }
            if (StringUtils.hasText(map.get("SignCity"))) { //合同签署城市

                signCity = map.get("SignCity");

            }
            if (StringUtils.hasText(map.get("SignArea"))) { //合同签署区县

                signArea = map.get("SignArea");

            }
            if (StringUtils.hasText(map.get("SignatureDate"))) { //合同生成日期/合同签署日期

                signatureDate = map.get("SignatureDate");

            }
            if (StringUtils.hasText(map.get("ZHXT01SINGLEloanPurpose")) || StringUtils.hasText(map.get("BorrowingUse2"))) { //赎楼借款用途
                final String detailZHXT01SINGLEloanPurpose = StringUtils.hasText(map.get("ZHXT01SINGLEloanPurpose")) ? map.get("ZHXT01SINGLEloanPurpose") : map.get("BorrowingUse2");

                switch (detailZHXT01SINGLEloanPurpose) {
                    case "0":
                        zHXT01SINGLEloanPurpose = "10";
                        break;
                    case "1":
                        zHXT01SINGLEloanPurpose = "8";
                        break;
                    case "3":
                        zHXT01SINGLEloanPurpose = "5";
                        break;
                    case "4":
                        zHXT01SINGLEloanPurpose = "7";
                        break;
                    default:
                        zHXT01SINGLEloanPurpose = "9";
                        break;
                }


            }

            if (StringUtils.hasText(map.get("ZHXT01SINGLEotherUse")) || StringUtils.hasText(map.get("BorrowingUse3"))) { //借款用途其他
                zHXT01SINGLEotherUse = StringUtils.hasText(map.get("ZHXT01SINGLEotherUse")) ? map.get("ZHXT01SINGLEotherUse") : map.get("BorrowingUse3");

            }

            if (StringUtils.hasText(map.get("ZHXT01SINGLEcontractCopies")) || StringUtils.hasText(map.get("ContractCopies"))) { //贷款合同份数
                contFileNum = StringUtils.hasText(map.get("ZHXT01SINGLEcontractCopies")) ? map.get("ZHXT01SINGLEcontractCopies") : map.get("ContractCopies");

            }

            FundContractDocReqest.PrdContInfo prdContInfo = new FundContractDocReqest.PrdContInfo();

            if ("15".equals(productDetailBo.getProductCode()) || "17".equals(productDetailBo.getProductCode()) || "28".equals(productDetailBo.getProductCode())) {
                prdContInfo.setPrdContType("1");
            } else if ("16".equals(productDetailBo.getProductCode())) {
                prdContInfo.setPrdContType("2");
            }
            prdContInfo.setPrdContVerNum(agreementBo.getTemplateId().toString());
            prdContInfo.setContProvince(signProvince);
            prdContInfo.setContCity(signCity);
            prdContInfo.setContArea(signArea);
            prdContInfo.setDyContSignDate(signatureDate);
            prdContInfo.setLoanPurpose(zHXT01SINGLEloanPurpose);
            if ("9".equals(zHXT01SINGLEloanPurpose)) {
                prdContInfo.setLoanPurposeOther(zHXT01SINGLEotherUse);
            }
            if (StringUtils.hasText(contFileNum)) {
                prdContInfo.setContFileNum(Integer.parseInt(contFileNum));
            }
            for (int i = 0; i < borrowerName.length; i++) {
                FundContractDocReqest.ApplyCustInfo applyCustInfo = new FundContractDocReqest.ApplyCustInfo();
                if (borrowerIdType.length > i && borrowerIdType.length > 0 && borrowerIdType[i] != null && !borrowerIdType[i].equals("NULL")) {
                    if (borrowerIdType[i].equals("4")) {
                        applyCustInfo.setRepLegalCustName(borrowerLegalPeople[i]);
                        applyCustInfo.setAppCertType("31");
                    } else {
                        if (borrowerName.length > i && borrowerName.length > 0 && borrowerName[i] != null && !borrowerName[i].equals("NULL")) {
                            applyCustInfo.setAppCustName(borrowerName[i]);
                        }
                        if (borroweridNo.length > i && borroweridNo.length > 0 && borroweridNo[i] != null && !borroweridNo[i].equals("NULL")) {
                            applyCustInfo.setAppCertNo(borroweridNo[i]);
                        }
                        if (borrowerPhone.length > i && borrowerPhone.length > 0 && borrowerPhone[i] != null && !borrowerPhone[i].equals("NULL")) {
                            applyCustInfo.setAppCustMobile(borrowerPhone[i]);
                        }
                        if (borrowerAddress.length > i && borrowerAddress.length > 0 && borrowerAddress[i] != null && !borrowerAddress[i].equals("NULL")) {
                            applyCustInfo.setAppCustAddr(borrowerAddress[i]);
                        }

                        switch (borrowerIdType[i]) {
                            case "0":
                                applyCustInfo.setAppCertType("20");
                                break; //身份证
                            case "1":
                                applyCustInfo.setAppCertType("22");
                                break; //护照
                            case "2":
                                applyCustInfo.setAppCertType("26");
                                break; //台湾居民来往大陆通行证
                            case "3":
                                applyCustInfo.setAppCertType("25");
                                break; //港澳居民来往内地通行证

                        }

                    }
                }
                applyCustInfoList.add(applyCustInfo);
            }
            contractNo = providerContractNo;
            fundContractDocReqVo.setPrdContNo(providerContractNo);
            fundContractDocReqVo.setApplyCustInfoList(applyCustInfoList);
            fundContractDocReqVo.setPrdContInfo(prdContInfo);

        } else if (TemplateType.MORTGAGE.getCode().equals(agreementBo.getTemplateType())) { //抵押
            fundContractDocReqVo.setCreatContType("dy");


            //抵押人信息
            String pledgerName[] = {}; //抵押人

            String pledgeridNo[] = {}; //抵押人证件号码

            String pledgerIdType[] = {}; //抵押人证件类型

            String pledgerPhone[] = {}; //抵押人联系方式

            String pledgerAddress[] = {}; //抵押人地址

            String pledgerLegalPeople[] = {}; //法定代表人

            String zHDYContractNo = ""; //抵押合同编号

            String signProvince = ""; //合同签署省份

            String signCity = ""; //合同签署城市

            String signArea = ""; //合同签署区县

            String borrowingAmount = ""; //借款金额

            String diYaWuCertificateNo = ""; //抵押物权属证编号

            String zHMortageHouseLocation = ""; //抵押物坐落详细地址

            String diYaContractCopies = ""; //抵押合同份数

            String signatureDate = ""; //合同生成日期/合同签署日期

            if (StringUtils.hasText(map.get("DiYaRenCertificateType"))) { //抵押人证件类型

                pledgerIdType = map.get("DiYaRenCertificateType").split(",");

            }
            if (StringUtils.hasText(map.get("DiYaRenName"))) { //抵押人姓名

                pledgerName = map.get("DiYaRenName").split(",");

            }
            if (StringUtils.hasText(map.get("DiYaRenContactInformation"))) { //抵押人联系方式

                pledgerPhone = map.get("DiYaRenContactInformation").split(",");

            }
            if (StringUtils.hasText(map.get("DiYaRenCertificateNumber"))) { //抵押人证件号码

                pledgeridNo = map.get("DiYaRenCertificateNumber").split(",");

            }
            if (StringUtils.hasText(map.get("DiYaRenContactAddress"))) { //抵押人地址

                pledgerAddress = map.get("DiYaRenContactAddress").split(",");

            }
            if (StringUtils.hasText(map.get("DiYaRenLegalPeople"))) { //法人姓名

                pledgerLegalPeople = map.get("DiYaRenLegalPeople").split(",");

            }
            if (StringUtils.hasText(map.get("SignProvince"))) { //合同签署省份

                signProvince = map.get("SignProvince");

            }
            if (StringUtils.hasText(map.get("SignCity"))) { //合同签署城市

                signCity = map.get("SignCity");

            }
            if (StringUtils.hasText(map.get("SignArea"))) { //合同签署区县

                signArea = map.get("SignArea");

            }
            if (StringUtils.hasText(map.get("ZHDYContractNo"))) { //抵押合同编号

                zHDYContractNo = map.get("ZHDYContractNo");

            }
            if (StringUtils.hasText(map.get("DiYaWuCertificateNo"))) { //抵押物权属证书编号

                diYaWuCertificateNo = map.get("DiYaWuCertificateNo");

            }
            if (StringUtils.hasText(map.get("ZHMortageHouseLocation"))) { //抵押物坐落详细地址

                zHMortageHouseLocation = map.get("ZHMortageHouseLocation");

            }
            if (StringUtils.hasText(map.get("DiYaContractCopies")) || StringUtils.hasText(map.get("ContractCopies"))) { //合同签署份数

                diYaContractCopies = StringUtils.hasText(map.get("DiYaContractCopies")) ? map.get("DiYaContractCopies") : map.get("ContractCopies");

            }
            if (StringUtils.hasText(map.get("BorrowingAmount"))) { //借款金额

                borrowingAmount = map.get("BorrowingAmount");

            }
            if (StringUtils.hasText(map.get("SignatureDate"))) { //合同生成日期/合同签署日期

                signatureDate = map.get("SignatureDate");

            }


            for (int i = 0; i < pledgerName.length; i++) {
                FundContractDocReqest.Mortgagor mortgagor = new FundContractDocReqest.Mortgagor();
                if (pledgerIdType.length > i && pledgerIdType.length > 0 && pledgerIdType[i] != null && !pledgerIdType[i].equals("NULL")) {
                    if (pledgerIdType[i].equals("4")) {
                        mortgagor.setDyRepLegalCustName(pledgerLegalPeople[i]);
                        mortgagor.setMortgagorCertType("31");
                    } else {
                        if (pledgerName.length > i && pledgerName.length > 0 && pledgerName[i] != null && !pledgerName[i].equals("NULL")) {
                            mortgagor.setMortgagorName(pledgerName[i]);
                        }
                        if (pledgeridNo.length > i && pledgeridNo.length > 0 && pledgeridNo[i] != null && !pledgeridNo[i].equals("NULL")) {
                            mortgagor.setMortgagorCertNo(pledgeridNo[i]);
                        }
                        if (pledgerPhone.length > i && pledgerPhone.length > 0 && pledgerPhone[i] != null && !pledgerPhone[i].equals("NULL")) {
                            mortgagor.setMortgagorMobile(pledgerPhone[i]);
                        }
                        if (pledgerAddress.length > i && pledgerAddress.length > 0 && pledgerAddress[i] != null && !pledgerAddress[i].equals("NULL")) {
                            mortgagor.setMortgagorAddr(pledgerAddress[i]);
                        }

                        switch (pledgerIdType[i]) {
                            case "0":
                                mortgagor.setMortgagorCertType("20");
                                break; //身份证
                            case "1":
                                mortgagor.setMortgagorCertType("22");
                                break; //护照
                            case "2":
                                mortgagor.setMortgagorCertType("26");
                                break; //台湾居民来往大陆通行证
                            case "3":
                                mortgagor.setMortgagorCertType("25");
                                break; //港澳居民来往内地通行证

                        }

                    }
                }
                mortgagorList.add(mortgagor);
            }

            FundContractDocReqest.DYContInfo dyContInfo = new FundContractDocReqest.DYContInfo();
            dyContInfo.setDyContVerNum(agreementBo.getTemplateId().toString());
            dyContInfo.setMortContractNo(zHDYContractNo);
            if (StringUtils.hasText(signatureDate)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.format(signatureDate);
            }
            dyContInfo.setDyContSignDate(signatureDate);
            dyContInfo.setDyContProvince(signProvince);
            dyContInfo.setDyContCity(signCity);
            dyContInfo.setDyContArea(signArea);
            dyContInfo.setDyLoanAmt(!StringUtils.hasText(borrowingAmount) ? new BigDecimal(0) : new BigDecimal(borrowingAmount));
            dyContInfo.setBuildingNo(diYaWuCertificateNo);
            dyContInfo.setBuildingAddr(zHMortageHouseLocation);
            dyContInfo.setDyContFileNum(StringUtils.hasText(diYaContractCopies) ? 0 : Integer.parseInt(diYaContractCopies));
            dyContInfo.setMortgagorNum(pledgerName.length);

            contractNo = zHDYContractNo;
            fundContractDocReqVo.setDyContInfo(dyContInfo);
            fundContractDocReqVo.setMortgagorList(mortgagorList);
        }


        if ("15".equals(productDetailBo.getProductCode()) || "17".equals(productDetailBo.getProductCode()) || "28".equals(productDetailBo.getProductCode())) {
            fundContractDocReqVo.setProductId("BKSLD001");
            fundContractDocReqVo.setProductName("赎楼");
        } else if ("16".equals(productDetailBo.getProductCode())) {
            fundContractDocReqVo.setProductId("BKWKD001");
            fundContractDocReqVo.setProductName("尾款");
        }

        String loanAmt = ""; //借款金额
        String loanTerm = ""; //借款期限，与"借款期限单位"配合使用
        String loanTermUnit = "D"; //D-按天、M-按月
        String contractRate = "0.102"; //借款利率 -年利率
        String receAccName = ""; //借款人银行卡账户名称
        String receAccNo = ""; //借款人银行卡账户
        String receAccBankName = ""; //银行卡开户行名称，比如建设银行
        String receAccBchName = ""; //开户行支行名称

        if (StringUtils.hasText(map.get("BorrowingAmount"))) { //借款金额

            loanAmt = map.get("BorrowingAmount");

        }
        if (StringUtils.hasText(map.get("BorrowingTime")) || StringUtils.hasText(map.get("ZHXT01SINGLEloanTerm"))) { //借款期限，与"借款期限单位"配合使用

            loanTerm = StringUtils.hasText(map.get("BorrowingTime")) ? map.get("BorrowingTime") : map.get("ZHXT01SINGLEloanTerm");

        }
        if (StringUtils.hasText(map.get("BorrowBankAccount")) || StringUtils.hasText(map.get("BorrowBankAccountZhu")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceAccNo")) || StringUtils.hasText(map.get("BankAccount"))) {
            //借款人银行卡账户

            receAccNo = StringUtils.hasText(map.get("BorrowBankAccount")) ? map.get("BorrowBankAccount") : StringUtils.hasText(map.get("BorrowBankAccountZhu")) ? map.get("BorrowBankAccountZhu")
                    : StringUtils.hasText(map.get("ZHXT01SINGLEreceAccNo")) ? map.get("ZHXT01SINGLEreceAccNo") : map.get("BankAccount");

        }
        if (StringUtils.hasText(map.get("BorrowBankAccountName")) || StringUtils.hasText(map.get("BorrowBankAccountNameZhu")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceAccName")) || StringUtils.hasText(map.get("BankAccountName"))) {
            //借款人银行卡账户名称

            receAccName = StringUtils.hasText(map.get("BorrowBankAccountName")) ? map.get("BorrowBankAccountName") : StringUtils.hasText(map.get("BorrowBankAccountNameZhu")) ? map.get("BorrowBankAccountNameZhu")
                    : StringUtils.hasText(map.get("ZHXT01SINGLEreceAccName")) ? map.get("ZHXT01SINGLEreceAccName") : map.get("BankAccountName");

        }
        if (StringUtils.hasText(map.get("BorrowBank")) || StringUtils.hasText(map.get("BorrowBankZhu")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceBankName")) || StringUtils.hasText(map.get("BankName"))) {
            //银行卡开户行名称，比如建设银行

            receAccBankName = StringUtils.hasText(map.get("BorrowBank")) ? map.get("BorrowBank") : StringUtils.hasText(map.get("BorrowBankZhu")) ? map.get("BorrowBankZhu")
                    : StringUtils.hasText(map.get("ZHXT01SINGLEreceBankName")) ? map.get("ZHXT01SINGLEreceBankName") : map.get("BankName");

        }
        if (StringUtils.hasText(map.get("BorrowBankBranch")) || StringUtils.hasText(map.get("BorrowBankBranchZhu")) || StringUtils.hasText(map.get("ZHXT01SINGLEreceBankBranch")) || StringUtils.hasText(map.get("BankBranch"))) {
            //开户行支行名称
            receAccBchName = StringUtils.hasText(map.get("BorrowBankBranch")) ? map.get("BorrowBankBranch") : StringUtils.hasText(map.get("BorrowBankBranchZhu")) ? map.get("BorrowBankBranchZhu")
                    : StringUtils.hasText(map.get("ZHXT01SINGLEreceBankBranch")) ? map.get("ZHXT01SINGLEreceBankBranch") : map.get("BankBranch");
        }


        fundContractDocReqVo.setFundLoanNo(fundLoanNo);
        fundContractDocReqVo.setProductLine("L_HOUSE_FINANCE");
        fundContractDocReqVo.setFinanceOrderNo(agreementBo.getLoanNo());
        fundContractDocReqVo.setPrdContNo(contractNo);
        fundContractDocReqVo.setLoanAmt(!StringUtils.hasText(loanAmt) ? new BigDecimal(0) : new BigDecimal(loanAmt));
        fundContractDocReqVo.setLoanTerm(!StringUtils.hasText(loanTerm) ? 0 : Integer.parseInt(loanTerm));
        fundContractDocReqVo.setLoanTermUnit(loanTermUnit);
        fundContractDocReqVo.setContractRate(new BigDecimal(contractRate));
        fundContractDocReqVo.setReceAccName(receAccName);
        fundContractDocReqVo.setReceAccNo(receAccNo);
        fundContractDocReqVo.setReceAccBankName(receAccBankName);
        fundContractDocReqVo.setReceAccBchName(receAccBchName);
        String notifyUrl = signNotifyUrl + "/api/1.0/accept-contract";
        fundContractDocReqVo.setNotifyUrl(notifyUrl);

        logger.info("【资金平台】[推送资金方信息] request -> {}", JSONConvert.toString(fundContractDocReqVo));
        String responseText = thirdPartyApiUtil.textPost(getFundContractDocUrl, JSONConvert.toString(fundContractDocReqVo));
        logger.info("【资金平台】[推送资金方信息] response -> {}", responseText);
        if (!StringUtils.hasText(responseText)) throw new BusinessException("调用资金平台失败！");
        FundInfoResponseBo responseBo = JSONConvert.fromString(FundInfoResponseBo.class, responseText);
        if (!"S0000".equals(responseBo.getReturnCode())) {
            throw new BusinessException("调用资金平台失败！" + responseBo.getReturnMsg());
        }
        return responseBo;
    }
}
