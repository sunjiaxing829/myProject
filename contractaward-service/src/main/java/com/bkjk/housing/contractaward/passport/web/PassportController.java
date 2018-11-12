package com.bkjk.housing.contractaward.passport.web;

import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.thirdcenter.passport.PassportService;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.web.annotation.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(description = "人员，权限相关接口")
@RestController
@LoginRequired
public class PassportController {
    @Inject
    private PassportService passportUtil;

    @ApiVersion("1.0")
    @ApiOperation(value = "当前登录顾问信息", httpMethod = "GET", notes = "当前登录顾问信息")
    @RequestMapping(value = "/advisor", method = RequestMethod.GET)
    public LoginUserBo queryAdvisor(HttpServletRequest request) {
        return passportUtil.getUser(request);
    }

    @ApiVersion("1.0")
    @ApiOperation(value = "获取下级", httpMethod = "GET", notes = "获取下级")
    @RequestMapping(value = "/under-advisor/{userId}", method = RequestMethod.GET)
    public List<LoginUserBo> queryUnderAdvisor(@PathVariable("userId") Long userId, @ApiParam(value = "ture:返回所有下级，false :返回直属下级") @RequestParam boolean all) {
        return passportUtil.queryUnderAdvisor(userId, all);
    }

}
