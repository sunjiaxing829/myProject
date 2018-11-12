package com.bkjk.housing.common.util.passport;


import com.bkjk.housing.common.util.passport.domain.OperationParticipantBo;
import com.bkjk.housing.common.util.passport.domain.RoleBo;
import com.bkjk.housing.common.util.passport.domain.UserBo;
import com.bkjk.platform.dubbox.annotation.DubboxConsumer;
import com.bkjk.platform.passport.api.facade.PassportFacade;
import com.bkjk.platform.passport.client.util.CookieUtil;
import com.bkjk.platform.uc.api.auth.UcAuthFacade;
import com.bkjk.platform.uc.api.org.EhrOrgFacade;
import com.bkjk.platform.uc.api.position.EhrPositionFacade;
import com.bkjk.platform.uc.api.user.EhrUserFacade;
import com.bkjk.platform.uc.dto.auth.AuthRoleDTO;
import com.bkjk.platform.uc.dto.org.EhrOrgDTO;
import com.bkjk.platform.uc.dto.position.UserPostioinDTO;
import com.bkjk.platform.uc.dto.user.EhrUserDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class PassportUtil {

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
    public EhrUserDTO getEhrInfo(HttpServletRequest request) {
        EhrUserDTO ehrUserDTO = null;
        try {
            String token = getToken(request);
            ehrUserDTO = passportFacade.loginByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ehrUserDTO;
    }


    //获取登录信息
    public UserBo getUser(HttpServletRequest request) {
        UserBo userBo = null;
        try {
            String token = getToken(request);
            EhrUserDTO ehrUserDTO = passportFacade.loginByToken(token);
            if (Objects.nonNull(ehrUserDTO)) {
                List<AuthRoleDTO> roleDTOList = ehrUserDTO.getRoleDTOList();
                userBo = new UserBo();
                userBo.setUserCode(ehrUserDTO.getUsercode());
                userBo.setUserName(ehrUserDTO.getName());
                userBo.setAccount(ehrUserDTO.getAccount());
                userBo.setEmail(ehrUserDTO.getEmail());
                userBo.setPhone(ehrUserDTO.getMobile());
                userBo.setAvatar(ehrUserDTO.getAvatar());
                userBo.setOrgCode(ehrUserDTO.getOrgCode());
                userBo.setOrgName(ehrUserDTO.getOrgName());
                userBo.setSuperiorCode(ehrUserDTO.getSuperiorCode());
                userBo.setSuperiorName(ehrUserDTO.getSuperiorName());
                userBo.setPositionCode(ehrUserDTO.getPositionCode());
                List<RoleBo> roleBos = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(roleDTOList)) {
                    for (AuthRoleDTO roleDto : roleDTOList) {
                        RoleBo roleBo = new RoleBo();
                        roleBo.setCityCode(roleDto.getCityCode());
                        roleBo.setCityName(roleDto.getCityName());
                        roleBo.setPositionCode(roleDto.getPositionCode());
                        roleBo.setRoleId(roleDto.getId());
                        roleBo.setType(roleDto.getType());
                        roleBo.setRoleName(roleDto.getRoleName());

                        roleBos.add(roleBo);
                    }
                }
                userBo.setRoles(roleBos);
                userBo.setRoleNames(this.getRoleNames(ehrUserDTO.getUsercode()));
            }
        } catch (Exception e) {
            log.error("调用passport-api获取用户信息报错：", e);
        }
        return userBo;
    }

    private String getToken(HttpServletRequest request) throws Exception {
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

    //获取城市code
    public String getCityCodeByRoleId(HttpServletRequest request) {

        UserBo user = getUser(request);

        String roleId = request.getParameter("clientRoleId");

        if (CollectionUtils.isNotEmpty(user.getRoles())) {
            log.info("当前登录人角色ID:{},当前登录人userCode:{}", roleId, user.getUserCode());
            for (RoleBo role : user.getRoles()) {
                if (role.getRoleId().equals(roleId)) {
                    return role.getCityCode();
                }
            }
        }

        return user.getRoles().get(0).getCityCode();
    }

    //获取所有下属
    public List<EhrUserDTO> getUnderUsers(String userCode) {
        List<EhrUserDTO> userDtos = ucAuthFacade.getUnderlingUserList(userCode);
        return userDtos;
    }

    //获取当前登录人roleName
    public String getRoleNames(String userCode) {
        List<UserPostioinDTO> userPositionins = ehrPositionFacade.getAllPostionByUsercode(userCode);
        String roleNames = null;
        if (CollectionUtils.isNotEmpty(userPositionins)) {
            StringBuffer roleBuffer = new StringBuffer();
            for (UserPostioinDTO userPostionin : userPositionins) {
                roleBuffer.append(userPostionin.getPositionName());
                roleBuffer.append(",");
            }
            roleNames = roleBuffer.toString().substring(0, roleBuffer.length() - 1);
        }
        return roleNames;
    }

    public UserBo getEhrUserByUserCode(String userCode) {
        EhrUserDTO ehrUserDTO = ehrUserFacade.getEhrUserByUserCode(userCode);
        UserBo userBo = null;
        if (Objects.nonNull(ehrUserDTO)) {
            userBo = new UserBo();
            userBo.setUserCode(ehrUserDTO.getUsercode());
            userBo.setUserName(ehrUserDTO.getName());
            userBo.setAccount(ehrUserDTO.getAccount());
            userBo.setEmail(ehrUserDTO.getEmail());
            userBo.setPhone(ehrUserDTO.getMobile());
            userBo.setAvatar(ehrUserDTO.getAvatar());
            userBo.setOrgCode(ehrUserDTO.getOrgCode());
            userBo.setOrgName(ehrUserDTO.getOrgName());
            userBo.setSuperiorCode(ehrUserDTO.getSuperiorCode());
            userBo.setSuperiorName(ehrUserDTO.getSuperiorName());
            userBo.setPositionCode(ehrUserDTO.getPositionCode());
        }
        return userBo;
    }

    public OperationParticipantBo getOperationInfo(HttpServletRequest request) {
        UserBo userBo = this.getUser(request);
        OperationParticipantBo operationParticipantBo = new OperationParticipantBo();
        operationParticipantBo.setOperationId(userBo.getUserCode());
        operationParticipantBo.setOperationName(userBo.getUserName());
        operationParticipantBo.setRoleName(userBo.getRoleNames());
        operationParticipantBo.setOrgName(userBo.getOrgName());
        return operationParticipantBo;
    }

    //获取所有下级顾问
    public List<String> getAllAdviser(UserBo user, List<String> listUserCode) {
        final List<String> advisers = Lists.newArrayList();
        try {
            final List<EhrUserDTO> listUser = ehrUserFacade.getAllUnderlingInfoList(user.getUserCode(), listUserCode);
            if (CollectionUtils.isNotEmpty(listUser)) {
                listUser.stream().forEach(ehrUserDTO -> {
                    advisers.add(ehrUserDTO.getUsercode());
                });
            }
        } catch (Exception e) {
            log.error("调用ud获取下级人员出错");
        }
        return advisers;
    }

    //获取组织机构
    public String getDepartment(String orgCode) {
        String treePathName = null;
        try {
            EhrOrgDTO orgBo = ehrOrgFacade.getEhrOrgByOrgCode(orgCode);
            if (orgBo != null && StringUtils.isNotEmpty(orgBo.getTreePathName())) {
                String[] treeArr = orgBo.getTreePathName().split("\\|");
                treePathName = treeArr[3] + "/" + treeArr[4] + "/" + treeArr[5];
            }
        } catch (Exception e) {
            log.error("根据orgCode获取ehrOrgByOrgCode异常");
        }
        return treePathName;
    }
}