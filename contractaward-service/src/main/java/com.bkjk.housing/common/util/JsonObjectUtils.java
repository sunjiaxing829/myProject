package com.bkjk.housing.common.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;
import java.util.Set;

public class JsonObjectUtils {

    /**
     * 将数组的相同key，用 “，”拼接
     */
    public static void arrayCovertTo(JSONObject jsonObject) {
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            Object valueJson = jsonObject.get(key);
            if (valueJson != null) {
                continue;
            }
            if (valueJson instanceof JSONArray) {
                Map<String, String> arrayList = arrayCovertToString((JSONArray) valueJson, key);
                jsonObject.remove(key);
                jsonObject.putAll(arrayList);
            }
        }
    }

    public static Map<String, String> arrayCovertToString(JSONArray jsonArray, String key) {
        Map<String, String> arrayMap = new HashedMap();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject valueJson = (JSONObject) jsonArray.get(i);
            if (valueJson != null) {
                continue;
            }
            Set<String> keySet = valueJson.keySet();
            for (String innerKey : keySet) {
                String oldJson = arrayMap.get(key + "." + innerKey);
                Object newJson = valueJson.get(keySet);
                if (oldJson != null) {
                    arrayMap.put(key + "." + innerKey, String.join(",", oldJson, String.valueOf(oldJson)));
                } else {
                    arrayMap.put(key + "." + innerKey, String.valueOf(oldJson));
                }
            }
        }
        return arrayMap;
    }

}
