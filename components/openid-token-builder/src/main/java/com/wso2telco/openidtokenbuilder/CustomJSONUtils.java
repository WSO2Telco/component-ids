package com.wso2telco.openidtokenbuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 *
 *
 */
public final class CustomJSONUtils {

    public static String buildJSON(Map<String, Object> params) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getKey() != null && !"".equals(param.getKey()) && param.getValue() != null && !""
                    .equals(param.getValue())) {
                jsonObject.put(param.getKey(), param.getValue());
            }
        }

        return jsonObject.toString();
    }

    public static Map<String, Object> parseJSON(String jsonBody) throws JSONException {

        Map<String, Object> params = new HashMap<String, Object>();
        JSONObject obj = new JSONObject(jsonBody);
        Iterator it = obj.keys();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof String) {
                String key = (String) o;
                params.put(key, obj.get(key));
            }
        }
        return params;
    }

}