package com.bkjk.housing.contractaward.api;

import com.bkjk.housing.common.constant.JsonConstants;
import com.bkjk.housing.common.util.BeanCopyUtil;
import com.bkjk.housing.contractaward.agreement.api.AgreementApi;
import com.bkjk.housing.contractaward.agreement.domain.CapitalPlatformBo;
import com.bkjk.housing.contractaward.api.vo.CapitalPlatformVo;
import com.bkjk.housing.contractaward.api.vo.JsonDto;
import com.bkjk.platform.logging.LoggerFactory;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@Api(description = "外部")
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Inject
    private AgreementApi agreementApi;

    @ApiVersion("1.0")
    @ApiOperation(value = "接收资金平台推送", httpMethod = "POST", notes = "接收资金平台推送")
    @RequestMapping(value = "/accept-contract", method = RequestMethod.POST)
    public JsonDto acceptContract(@RequestBody CapitalPlatformVo capitalPlatformVo) {
        JsonDto jsonData = new JsonDto();
        try {
            agreementApi.acceptContract(BeanCopyUtil.copy(capitalPlatformVo, CapitalPlatformBo.class));
        } catch (Exception e) {
            LOGGER.error("接收资金平台推送报错：", e);
            jsonData.setCode(JsonConstants.FAILE);
            jsonData.setMsg(JsonConstants.ERROR_UNKNOW_INFO);
        }
        jsonData.setCode(JsonConstants.OK);
        jsonData.setMsg(JsonConstants.OK_INFO);
        return jsonData;
    }
}
