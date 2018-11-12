package com.bkjk.housing.common.util;

import com.bkjk.platform.logging.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeanCopyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCopyUtil.class);

    private BeanCopyUtil() {
    }

    /**
     * Bean Copy 列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        List<T> targetList = new ArrayList<T>();
        if (!CollectionUtils.isEmpty(sourceList)) {
            Iterator<S> var = sourceList.iterator();
            while (var.hasNext()) {
                Object s = var.next();
                try {
                    T t = targetClass.getDeclaredConstructor().newInstance();
                    BeanUtils.copyProperties(s, t);
                    targetList.add(t);
                } catch (Exception e) {
                    LOGGER.error("BeanCopyUtils is error ", e);
                }
            }
        }
        return targetList;
    }

    /**
     * 单个对象copay
     *
     * @param s           源数据，不能为空
     * @param targetClass 目标对象 class
     * @return 目标结构
     */
    public static <T, S> T copy(S s, Class<T> targetClass) {
        if (s == null) {
            return null;
        }
        T target = null;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(s, target);
        } catch (Exception e) {
            LOGGER.error("BeanCopyUtil is error  ", e);
        }
        return target;
    }
}
