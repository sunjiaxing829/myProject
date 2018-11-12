package com.bkjk.housing.contractaward.contract.web;

import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.thirdcenter.loan.LoanService;
import com.bkjk.housing.common.thirdcenter.passport.PassportService;
import com.bkjk.housing.common.util.PrivilegeUtil;
import com.bkjk.housing.contractaward.contract.api.ContractInfoApi;
import com.bkjk.housing.contractaward.contract.domain.ContractInfoBo;
import com.bkjk.housing.contractaward.contract.domain.ContractSearchBo;
import com.bkjk.housing.contractaward.contract.vo.ContractSearchVo;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.logging.LoggerFactory;
import com.bkjk.platform.orm.mybatis.segment.PageSegment;
import com.bkjk.platform.uc.dto.user.EhrUserDTO;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RestController
//@LoginRequired
@Api(description = "合同相关接口")
public class ContractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractController.class);

    @Inject
    private ContractInfoApi contractApi;

    @Inject
    private PassportService passportUtil;

    @Inject
    private LoanService loanService;

    @Inject
    private PrivilegeUtil privilegeUtil;

    @ApiVersion("1.0")
    @ApiOperation(value = "查询合同列表(分页)", httpMethod = "POST", notes = "查询合同列表(分页)")
    @RequestMapping(value = "/contracts", method = RequestMethod.POST)
    public PageSegment<ContractInfoBo> getContractsPage(@RequestBody ContractSearchVo contractSearchVo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportUtil.getUser(request);
        ContractSearchBo contractSearchBo = new ContractSearchBo();
        BeanUtils.copyProperties(contractSearchVo, contractSearchBo);
        if (!privilegeUtil.checkSuperMan(loginUserBo)) {
            contractSearchBo.setUserCodeList(passportUtil.getUserCodeListByLevel(loginUserBo));
        }
        return contractApi.getContractsPage(contractSearchBo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "查询合同详情", httpMethod = "GET", notes = "通过 loanNo 查询合同详情")
    @RequestMapping(value = "/contract/{loanNo}", method = RequestMethod.GET)
    public ContractInfoBo getContractDetailByNo(@ApiParam("进件号") @PathVariable("loanNo") String loanNo) {
        ContractInfoBo contractInfoBo = contractApi.getContractDetailByNo(loanNo);
        if (contractInfoBo != null && contractInfoBo.getSignUserId() != null) {
            EhrUserDTO ehrUserDTO = passportUtil.getUserByCode(String.valueOf(contractInfoBo.getSignUserId()));
            contractInfoBo.setSignUserName(ehrUserDTO.getName());
            contractInfoBo.setAdvisorOrgName(ehrUserDTO.getOrgName());
        }
        return contractInfoBo;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "合同设置无效", httpMethod = "GET", notes = "合同设置无效")
    @RequestMapping(value = "/invalid-contract/{loanNo}", method = RequestMethod.GET)
    public boolean invalidContract(@ApiParam("进件号") @PathVariable("loanNo") String loanNo) {
        loanService.contractInvalid(loanNo);
        contractApi.invalidContract(loanNo);
        return true;
    }
}
