package com.bkjk.housing.common.thirdcenter.crm;

import com.bkjk.housing.crm.customer.api.CustomerInteractiveApi;
import com.bkjk.housing.crm.customer.domain.CustomerContractChangeBo;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class CrmService {
    private final Logger logger = LoggerFactory.getLogger(CrmService.class);

    @Inject
    private CustomerInteractiveApi customerInteractiveApi;

    /**
     * 推送合同状态信息
     *
     * @param loanNo
     * @param demandId
     * @param status
     */
    public void changeContract(String loanNo, Long demandId, Integer status) {
        CustomerContractChangeBo changeBo = new CustomerContractChangeBo();
        changeBo.setContractNo(loanNo);
        changeBo.setDemandId(demandId);
        changeBo.setContractStatus(status);
        logger.info("【CRM】[推送合同状态信息] request -> {},{}", loanNo, JSONConvert.toString(changeBo));
        boolean flag = customerInteractiveApi.contractChange(changeBo);
        logger.info("【CRM】[推送合同状态信息] response -> {}, {}", loanNo, flag);
    }
}
