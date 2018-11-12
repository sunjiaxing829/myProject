package com.bkjk.housing.common.util;

import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.devtools.util.converter.JSONConvert;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;
import java.util.stream.Collectors;

public class FieldUtil {

    /**
     * 填充模板字段parameters
     * @param parametersJson
     * @param editorMap
     * @return
     */
    public static String editorMapToParameters(String parametersJson, Map<String, String> editorMap) {
        Map<String, Object> map = JSONConvert.fromString(Map.class, parametersJson);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Objects.nonNull(entry.getValue())) continue;
            if (entry.getValue() instanceof List) {
                List<Object> list = (List) entry.getValue();
                Map<String, String> childMap = (Map) list.get(0);
                int size = 0;
                for (Map.Entry<String, String> childEntry : childMap.entrySet()) {
                    String value = editorMap.get(childEntry.getKey());
                    if (StringUtils.hasText(value)) {
                        size = value.split(",").length;
                        break;
                    }
                }
                List<Map<String, String>> records = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    Map<String, String> m = new HashMap<>();
                    for (Map.Entry<String, String> childEntry : childMap.entrySet()) {
                        String value = editorMap.get(childEntry.getKey());
                        if (StringUtils.hasText(value)) {
                            m.put(childEntry.getKey(), value.split(",")[i]);
                        } else {
                            m.put(childEntry.getKey(), value);
                        }
                    }
                    records.add(m);
                }
                map.put(entry.getKey(), JSONConvert.toString(records, List.class));
            } else {
                map.put(entry.getKey(), editorMap.get(entry.getKey()));
            }
        }
        return JSONConvert.toString(map, Map.class);
    }


    /**
     * parameters 模板字段转为 Map
     * @param parameters
     * @return
     */
    public static Map<String, String> parametersJsonToMap(String parameters) {
        Map<String, String> fieldsMap = new HashedMap();
        Map<String, Object> map = JSONConvert.fromString(Map.class, parameters);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof List) {
                List<Object> list = (List) entry.getValue();
                Map<String, List<String>> listMap = new HashMap<>();
                for (Object object : list) {
                    Map<String, String> childMap = (Map) object;
                    for (Map.Entry<String, String> childMapEntry : childMap.entrySet()) {
                        if (listMap.containsKey(childMapEntry.getKey())) {
                            List<String> oldList = listMap.get(childMapEntry.getKey());
                            oldList.add(childMapEntry.getValue());
                        } else {
                            List<String> newList = new ArrayList<>();
                            newList.add(childMapEntry.getValue());
                            listMap.put(childMapEntry.getKey(), newList);
                        }
                    }
                }
                for (Map.Entry<String, List<String>> childMapEntry : listMap.entrySet()) {
                    fieldsMap.put(childMapEntry.getKey(), childMapEntry.getValue().stream()
                            .collect(Collectors.joining(",")));
                }
            } else {
                fieldsMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return fieldsMap;
    }
}
