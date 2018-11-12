package com.bkjk.housing.contractaward.agreement.web;

import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.contractaward.agreement.api.ContractImageApi;
import com.bkjk.housing.contractaward.agreement.domain.AgreementBo;
import com.bkjk.housing.contractaward.agreement.service.ContractImageService;
import com.bkjk.housing.contractaward.agreement.vo.ContractImageVo;
import com.bkjk.platform.exception.BusinessException;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

@RestController
@LoginRequired
@Api(description = "合同电子件")
public class ContractImageController {

    @Inject
    private ContractImageService contractImageService;


    @Inject
    private ContractImageApi contractImageApi;

    @ApiVersion("1.0")
    @ApiOperation(value = "上传合同电子电子件", httpMethod = "PUT", notes = "上传合同电子电子件")
    @RequestMapping(value = "/upload-agreementimage", method = RequestMethod.PUT)
    public Boolean batchSaveContractImage(@RequestBody List<ContractImageVo> contractImageVoList) {
        if (CollectionUtils.isEmpty(contractImageVoList)) {
            throw new BusinessException("电子件不能为空");
        }
        return contractImageService.batchSaveContractImage(contractImageVoList);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "查询合同电子件", httpMethod = "PUT", notes = "查询合同电子件(多模版返回所有主协议及电子件)-app使用")
    @RequestMapping(value = "/agreementimage/{agreementNo}", method = RequestMethod.GET)
    public List<AgreementBo> getContractImageList(@PathVariable("agreementNo") String agreementNo) {
        return contractImageApi.getContractImageList(agreementNo);
    }
}
