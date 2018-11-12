package com.bkjk.housing.common.interceptor;

import com.bkjk.housing.common.annotation.LoginRequired;
import com.bkjk.housing.common.util.BaseResult;
import com.bkjk.housing.common.util.passport.PassportUtil;

import com.bkjk.housing.common.util.passport.domain.UserBo;
import com.bkjk.platform.context.SpringContext;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class LoginInterceptor extends BaseInterceptor {

    private static final String AUTHORIZATIONKEY = "Authorization";

    private final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    private PassportUtil passportUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!SpringContext.isSiteController(handler)) return Boolean.TRUE;
        final LoginRequired loginRequired = this.getAnnotation(handler, LoginRequired.class);
        if (Objects.isNull(loginRequired)) return Boolean.TRUE;
        if (!hasLogin(request)) {
            if (StringUtils.hasText(request.getHeader(AUTHORIZATIONKEY))) {
                response.getWriter().write(JSONConvert.toString(BaseResult.UNAUTHORIZED, BaseResult.class));
                return Boolean.FALSE;
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean hasLogin(HttpServletRequest request) {
        UserBo user = null;
        try {
            user = passportUtil.getUser(request);
        } catch (Exception e) {
            logger.error("登陆拦截器获取用户信息报错：", e);
        }
        return null != user;
    }

    @Inject
    public void setPassportUtil(PassportUtil passportUtil) {
        this.passportUtil = passportUtil;
    }

}