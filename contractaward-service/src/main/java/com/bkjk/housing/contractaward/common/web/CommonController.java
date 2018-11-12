package com.bkjk.housing.contractaward.common.web;

import com.bkjk.housing.classic.combination.api.CombinationExternalApi;
import com.bkjk.housing.classic.combination.api.domain.CombinationProductBriefBo;
import com.bkjk.housing.classic.combination.api.domain.CombinationProductBriefSearchBo;
import com.bkjk.housing.classic.field.api.IncomingTemplateApi;
import com.bkjk.housing.classic.field.api.domain.FieldPoolBo;
import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.enums.BusinessTypeEnum;
import com.bkjk.housing.common.thirdcenter.passport.PassportService;
import com.bkjk.housing.contractaward.common.api.CommonApi;
import com.bkjk.housing.contractaward.common.domain.BankDictBo;
import com.bkjk.housing.contractaward.common.domain.CodeNameResponseBo;
import com.bkjk.housing.contractaward.common.domain.FieldMappingBo;
import com.bkjk.housing.contractaward.common.vo.FieldMappingVo;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.contract.template.api.TemplateFieldApi;
import com.bkjk.platform.contract.template.domain.TemplateFieldBo;
import com.bkjk.platform.orm.mybatis.segment.PageSegment;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@LoginRequired
@Api(description = "公共配置，组件")
public class CommonController {

    @Inject
    private CommonApi commonApi;

    @Inject
    private IncomingTemplateApi incomingTemplateApi;

    @Inject
    private TemplateFieldApi templateFieldApi;

    @Inject
    private CombinationExternalApi combinationExternalApi;

    @Inject
    private PassportService passportService;

    @ApiOperation(value = "获取银行字典", notes = "Owner : 孙佳兴  Description : 获取银行字典", httpMethod = "GET")
    @ApiVersion("1.0")
    @RequestMapping(value = "/banks", method = RequestMethod.GET)
    public List<BankDictBo> banks() {
        return commonApi.getBanks();
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "查询所有产品列表", httpMethod = "GET", notes = "查询所有产品列表")
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public List<CodeNameResponseBo> products(HttpServletRequest request) {
        List<CodeNameResponseBo> records = new ArrayList<>();
        LoginUserBo loginUserBo = passportService.getUser(request);
        CombinationProductBriefSearchBo searchBo = new CombinationProductBriefSearchBo();
        searchBo.setCityCode(loginUserBo.getCityCode());
        List<CombinationProductBriefBo> productBos = combinationExternalApi.listCombinationProductBriefAfterOnlineByCondition(searchBo);
        if (CollectionUtils.isNotEmpty(productBos)) {
            productBos.stream().forEach(productBo -> {
                CodeNameResponseBo responseBo = new CodeNameResponseBo();
                responseBo.setCode(productBo.getCombinationCode());
                responseBo.setName(productBo.getCombinationName());
                records.add(responseBo);
            });
        }
        return records;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "查询所有协议类型", httpMethod = "GET", notes = "查询所有协议类型")
    @RequestMapping(value = "/business-types", method = RequestMethod.GET)
    public List<CodeNameResponseBo> businessTypes() {
        List<CodeNameResponseBo> records = new ArrayList<>();
        for (BusinessTypeEnum businessTypeEnum : BusinessTypeEnum.values()) {
            CodeNameResponseBo responseBo = new CodeNameResponseBo();
            responseBo.setCode(businessTypeEnum.getCode());
            responseBo.setName(businessTypeEnum.getDescription());
            records.add(responseBo);
        }
        return records;
    }

    @ApiOperation(value = "获取所有进件字段", httpMethod = "GET", notes = "获取所有进件字段")
    @ApiVersion("1.0")
    @RequestMapping(value = "/loanfields", method = RequestMethod.GET)
    public List<CodeNameResponseBo> loanFields() {
        List<CodeNameResponseBo> records = new ArrayList<>();
        List<FieldPoolBo> fieldPoolBos = incomingTemplateApi.getByModuleId(null);
        if (CollectionUtils.isNotEmpty(fieldPoolBos)) {
            fieldPoolBos.stream().forEach(fieldPoolBo -> {
                CodeNameResponseBo responseBo = new CodeNameResponseBo();
                responseBo.setCode(fieldPoolBo.getFieldId());
                responseBo.setName(fieldPoolBo.getFieldName());
                records.add(responseBo);
            });
        }
        return records;
    }

    @ApiOperation(value = "获取所有合同模版字段", httpMethod = "GET", notes = "获取所有合同模版字段")
    @ApiVersion("1.0")
    @RequestMapping(value = "/contractfields", method = RequestMethod.GET)
    public List<CodeNameResponseBo> contractFields() {
        List<CodeNameResponseBo> records = new ArrayList<>();
        List<TemplateFieldBo> templateFieldBos = templateFieldApi.gets();
        if (CollectionUtils.isNotEmpty(templateFieldBos)) {
            templateFieldBos.stream().forEach(templateFieldBo -> {
                CodeNameResponseBo responseBo = new CodeNameResponseBo();
                responseBo.setCode(templateFieldBo.getCode());
                responseBo.setName(templateFieldBo.getName());
                records.add(responseBo);
            });
        }
        return records;
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "新增或修改字段绑定", httpMethod = "PUT", notes = "新增或修改字段绑定")
    @RequestMapping(value = "/fieldmapping", method = RequestMethod.PUT)
    public FieldMappingBo addOrUpdateFieldMapping(@RequestBody FieldMappingVo fieldMappingVo) {
        FieldMappingBo fieldMappingBo = new FieldMappingBo();
        BeanUtils.copyProperties(fieldMappingVo, fieldMappingBo);
        return commonApi.addOrUpdateFieldMapping(fieldMappingBo);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取字段配置列表", httpMethod = "POST", notes = "获取字段配置列表")
    @RequestMapping(value = "/fieldmappings", method = RequestMethod.POST)
    public PageSegment<FieldMappingBo> getFieldMappings(@RequestBody FieldMappingVo fieldMappingVo) {
        FieldMappingBo fieldMappingBo = new FieldMappingBo();
        BeanUtils.copyProperties(fieldMappingVo, fieldMappingBo);
        return commonApi.getFieldMappings(fieldMappingBo);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "删除字段配置列表", httpMethod = "DELETE", notes = "删除字段配置列表")
    @RequestMapping(value = "/fieldmapping/{id}", method = RequestMethod.DELETE)
    public boolean deleteFieldMapping(@PathVariable("id") Long id) {
        return commonApi.deleteFieldMapping(id);
    }
}