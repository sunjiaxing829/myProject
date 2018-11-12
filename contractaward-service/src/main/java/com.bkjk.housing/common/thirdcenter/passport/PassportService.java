package com.bkjk.housing.common.thirdcenter.passport;

import com.bkjk.housing.common.enums.PositionLevelEnum;
import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import com.bkjk.platform.dubbox.annotation.DubboxConsumer;
import com.bkjk.platform.logging.LoggerFactory;
import com.bkjk.platform.passport.api.facade.PassportFacade;
import com.bkjk.platform.passport.client.util.CookieUtil;
import com.bkjk.platform.uc.api.auth.UcAuthFacade;
import com.bkjk.platform.uc.api.org.EhrOrgFacade;
import com.bkjk.platform.uc.api.position.EhrPositionFacade;
import com.bkjk.platform.uc.api.user.EhrUserFacade;
import com.bkjk.platform.uc.dto.position.UserPostioinDTO;
import com.bkjk.platform.uc.dto.user.EhrUserDTO;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PassportService {

    private final Logger logger = LoggerFactory.getLogger(PassportService.class);

    @DubboxConsumer(check = false)
    private PassportFacade passportFacade;

    @DubboxConsumer(check = false)
    private UcAuthFacade ucAuthFacade;

    @DubboxConsumer(check = false)
    private EhrOrgFacade ehrOrgFacade;

    @DubboxConsumer(check = false)
    private EhrUserFacade ehrUserFacade;

    @DubboxConsumer(check = false)
    private EhrPositionFacade ehrPositionFacade;

    /**
     * 获取登录信息
     *
     * @param request
     * @return
     */
    public LoginUserBo getUser(HttpServletRequest request) {
        LoginUserBo userBo = null;
        try {
            String token = getDecodeToken(request);
            EhrUserDTO ehrUserDTO = passportFacade.loginByToken(token);
            if (ehrUserDTO != null) {
                logger.info("当前登陆人信息：user = {}", JSONConvert.toString(ehrUserDTO, EhrUserDTO.class));
                userBo = new LoginUserBo();
                userBo.setUserId(Long.valueOf(ehrUserDTO.getUsercode()));
                userBo.setUserName(ehrUserDTO.getName());
                userBo.setAccount(ehrUserDTO.getAccount());
                userBo.setEmail(ehrUserDTO.getEmail());
                userBo.setPhone(ehrUserDTO.getMobile());
                userBo.setAvatar(ehrUserDTO.getAvatar());
                userBo.setOrgCode(ehrUserDTO.getOrgCode());
                userBo.setOrgName(ehrUserDTO.getOrgName());
                userBo.setSuperiorCode(ehrUserDTO.getSuperiorCode() == null ? null : Long.valueOf(ehrUserDTO.getSuperiorCode()));
                userBo.setSuperiorName(ehrUserDTO.getSuperiorName());
                userBo.setPositionCode(ehrUserDTO.getPositionCode());
                userBo.setPositionName(ehrUserDTO.getPositionName());
                userBo.setPositionLevel(ehrUserDTO.getPositionLevel());
                String cityCode = null;
                List<UserPostioinDTO> userPostioinDTOs = ehrUserDTO.getUserPositionDTOList();
                //循环找城市
                if (CollectionUtils.isNotEmpty(userPostioinDTOs)) {
                    for (UserPostioinDTO userPostioinDTO : userPostioinDTOs) {
                        if (userBo.getPositionCode().equals(userPostioinDTO.getPositionCode())) {
                            cityCode = userPostioinDTO.getCityCode();
                            break;
                        }
                    }
                }
                userBo.setCityCode(cityCode);
            }
        } catch (Exception e) {
            logger.error("调用passport-api获取用户信息报错：", e);
        }
        return userBo;
    }

    /**
     * 获取token
     *
     * @param request
     * @return
     * @throws Exception
     */
    private String getDecodeToken(HttpServletRequest request) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null) {
            token = request.getParameter("uid");
            if (token == null) {
                token = CookieUtil.readAndDecodeCookie(request);
                CookieUtil.readCookie(request, "bkjk_pin");
            }
        }
        return token;
    }

    /**
     * 通过职级获取下属编号
     *
     * @param userBo
     * @return
     */
    public List<Long> getUserCodeListByLevel(LoginUserBo userBo) {
        if (PositionLevelEnum.ATTACHE.getLevelCode().equals(userBo.getPositionLevel())) {
            return Arrays.asList(userBo.getUserId());
        }
        List<Long> underUserList = new ArrayList<>();
        List<EhrUserDTO> userDtos = ucAuthFacade.getUnderlingUserList(userBo.getUserId().toString());
        if (CollectionUtils.isNotEmpty(userDtos)) {
            for (EhrUserDTO userDto : userDtos) {
                underUserList.add(Long.valueOf(userDto.getUsercode()));
            }
        }
        underUserList.add(userBo.getUserId());
        return underUserList;
    }

    /**
     * 获取下级(all = ture 就返回所有下级，否则返回直属下级)
     *
     * @param userId
     * @param all
     * @return
     */
    public List<LoginUserBo> queryUnderAdvisor(Long userId, boolean all) {
        List<LoginUserBo> underUserList = new ArrayList<>();
        List<EhrUserDTO> ehrUserDTOs;
        if (all) {
            ehrUserDTOs = ucAuthFacade.getUnderlingUserList(userId.toString());
        } else {
            ehrUserDTOs = ehrUserFacade.getUnderlingOneLeveInfoList(userId.toString());
        }
        if (CollectionUtils.isNotEmpty(ehrUserDTOs)) {
            for (EhrUserDTO ehrUserDTO : ehrUserDTOs) {
                LoginUserBo userBo = new LoginUserBo();
                userBo.setUserId(Long.valueOf(ehrUserDTO.getUsercode()));
                userBo.setUserName(ehrUserDTO.getName());
                userBo.setAccount(ehrUserDTO.getAccount());
                userBo.setEmail(ehrUserDTO.getEmail());
                userBo.setPhone(ehrUserDTO.getMobile());
                userBo.setAvatar(ehrUserDTO.getAvatar());
                userBo.setOrgCode(ehrUserDTO.getOrgCode());
                userBo.setOrgName(ehrUserDTO.getOrgName());
                userBo.setSuperiorCode(ehrUserDTO.getSuperiorCode() == null ? null : Long.valueOf(ehrUserDTO.getSuperiorCode()));
                userBo.setSuperiorName(ehrUserDTO.getSuperiorName());
                userBo.setPositionCode(ehrUserDTO.getPositionCode());
                userBo.setPositionName(ehrUserDTO.getPositionName());
                userBo.setPositionLevel(ehrUserDTO.getPositionLevel());
                underUserList.add(userBo);
            }
        }
        return underUserList;
    }

    /**
     * 通过userCodeList 获取用户列表
     */
    public List<EhrUserDTO> getUserByCodeList(List<String> userCodeList) {
        return this.ehrUserFacade.getEhrUserByUserCodes(userCodeList);
    }

    public List<EhrUserDTO> getUserAllSuperior(String userCode) {
        EhrUserDTO ehrUserDTO = this.ehrUserFacade.getEhrUserByUserCode(userCode);
        return this.ehrUserFacade.getAllSuperiorList(ehrUserDTO.getUsercode(), ehrUserDTO.getPositionCode());
    }

    public EhrUserDTO getUserByCode(String userCode) {
        return this.ehrUserFacade.getEhrUserByUserCode(userCode);
    }
}