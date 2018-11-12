package com.bkjk.housing.contractaward.contract.service;

import com.bkjk.housing.common.thirdcenter.finance.FinanceService;
import com.bkjk.housing.contractaward.contract.api.ContractInfoApi;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ContractService {

    @Inject
    private FinanceService financeService;

    @Inject
    private ContractInfoApi contractInfoApi;
}
