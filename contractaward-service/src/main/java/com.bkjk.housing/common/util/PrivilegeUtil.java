package com.bkjk.housing.common.util;

import com.bkjk.housing.contractaward.passport.domain.LoginUserBo;
import com.bkjk.platform.uc.dto.user.EhrUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PrivilegeUtil {

    @Value("${com.bkjk.housing.privilege.userCodes}")
    private String userCodes;

    @Value("${com.bkjk.housing.privilege.positionCodes}")
    private String positionCodes;

    @Value("${com.bkjk.housing.privilege.areaManagerCode}")
    private String areaManagerCode;

    //获取系统号
    private Set<Long> getUserCodeSet() {
        Set<Long> userCodeSet = new HashSet<>();
        if (StringUtils.hasText(userCodes)) {
            String[] users = userCodes.split(",");
            for (int i = 0; i < users.length; i++) {
                userCodeSet.add(Long.valueOf(users[i]));
            }
        }
        userCodeSet.add(20366682l);
        return userCodeSet;
    }

    //获取职务号
    private Set<String> getPositionCodeSet() {
        Set<String> positionCodeSet = new HashSet<>();
        if (StringUtils.hasText(positionCodes)) {
            String[] positions = positionCodes.split(",");
            for (int i = 0; i < positions.length; i++) {
                positionCodeSet.add(positions[i]);
            }
        }
        positionCodeSet.add("121500");
        positionCodeSet.add("121510");
        positionCodeSet.add("120740");
        positionCodeSet.add("123525");
        positionCodeSet.add("123735");
        positionCodeSet.add("123425");
        return positionCodeSet;
    }

    //大区副总
    private Set<String> getAreaManager() {
        Set<String> areaManagerUserCodeSet = new HashSet<>();
        if (StringUtils.hasText(areaManagerCode)) {
            String[] positions = areaManagerCode.split(",");
            for (int i = 0; i < positions.length; i++) {
                areaManagerUserCodeSet.add(positions[i]);
            }
        }
        return areaManagerUserCodeSet;
    }

    public Boolean checkSuperMan(LoginUserBo loginUserBo) {
        Set<Long> userCodeSet = getUserCodeSet();
        Set<String> positionCodeSet = getPositionCodeSet();
        return loginUserBo.getPositionName().contains("法务") || userCodeSet.contains(loginUserBo.getUserId()) || positionCodeSet.contains(loginUserBo.getPositionCode());
    }

    public EhrUserDTO filterAreaManager(List<EhrUserDTO> ehrUserDTOList) {
        Set<String> areaManagerUserCodeSet = getAreaManager();
        for (EhrUserDTO ehrUserDTO : ehrUserDTOList) {
            if (areaManagerUserCodeSet.contains(ehrUserDTO.getPositionCode())) return ehrUserDTO;
        }
        return null;
    }
}
