package com.bkjk.housing.contractaward.agreement.web;

import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.thirdcenter.passport.PassportService;
import com.bkjk.housing.common.util.PrivilegeUtil;
import com.bkjk.housing.contractaward.agreement.domain.AuditAgreementBo;
import com.bkjk.housing.contractaward.agreement.service.AgreementService;
import com.bkjk.housing.contractaward.agreement.vo.AgreementExamainVo;
import com.bkjk.housing.contractaward.examine.api.AgreementExamineApi;
import com.bkjk.housing.contractaward.examine.domain.ApproveExamineBo;
import com.bkjk.housing.contractaward.examine.domain.RejectExamineBo;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.uc.dto.user.EhrUserDTO;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController
@LoginRequired
@Api(description = "提审相关接口")
public class AgreementExamineController {

    @Inject
    private AgreementService agreementService;

    @Inject
    private AgreementExamineApi agreementExamineApi;

    @Inject
    private PassportService passportService;

    @Inject
    private PrivilegeUtil privilegeUtil;

    @ApiVersion("1.0")
    @ApiOperation(value = "提审", httpMethod = "GET", notes = "提审")
    @RequestMapping(value = "/audit-agreement/{agreementNo}", method = RequestMethod.GET)
    public boolean auditAgreement(@ApiParam("协议编号") @PathVariable("agreementNo") String agreementNo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        AuditAgreementBo auditAgreementBo = new AuditAgreementBo();
        auditAgreementBo.setAgreementNo(agreementNo);
        auditAgreementBo.setCreatedId(loginUserBo.getUserId());
        auditAgreementBo.setCreatedName(loginUserBo.getUserName());
        auditAgreementBo.setSuperiorCode(loginUserBo.getSuperiorCode());
        auditAgreementBo.setSuperiorName(loginUserBo.getSuperiorName());
        return agreementService.auditAgreement(auditAgreementBo);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "驳回审批请求", notes = "驳回审批请求")
    @RequestMapping(value = "/reject-agreement", method = RequestMethod.POST)
    public Boolean rejectAgreement(@RequestBody AgreementExamainVo agreementExamainVo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        RejectExamineBo rejectExamineBo = new RejectExamineBo();
        BeanUtils.copyProperties(agreementExamainVo, rejectExamineBo);
        rejectExamineBo.setUserCode(loginUserBo.getUserId());
        rejectExamineBo.setUserName(loginUserBo.getUserName());
        return agreementExamineApi.rejectAgreement(rejectExamineBo);
    }


    @ApiVersion("1.0")
    @ApiOperation(value = "同意审批请求", notes = "同意审批请求")
    @RequestMapping(value = "/approve-agreement", method = RequestMethod.POST)
    public Boolean approveAgreement(@RequestBody AgreementExamainVo agreementExamainVo, HttpServletRequest request) {
        LoginUserBo loginUserBo = passportService.getUser(request);
        ApproveExamineBo approveExamineBo = this.agreementExamineApi.getAgreementExamineByAgreementNo(agreementExamainVo.getAgreementNo());
        approveExamineBo.setUserCode(loginUserBo.getUserId());
        approveExamineBo.setUserName(loginUserBo.getUserName());
        if ("AREAMANAGER".equals(approveExamineBo.getNextFlowCode())) {
            List<EhrUserDTO> ehrUserDTOList = this.passportService.getUserAllSuperior(String.valueOf(approveExamineBo.getCreatedId()));
            EhrUserDTO ehrUserDTO = privilegeUtil.filterAreaManager(ehrUserDTOList);
            if (Objects.nonNull(ehrUserDTO)) {
                approveExamineBo.setNextUserCode(Long.valueOf(ehrUserDTO.getUsercode()));
                approveExamineBo.setNextUserName(ehrUserDTO.getName());
            }
        }
        return agreementExamineApi.approveAgreement(approveExamineBo);
    }

}
