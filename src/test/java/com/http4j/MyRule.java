package com.http4j;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class MyRule implements ResultRule {

    JSONObject jsonObject = null;


    @Override
    public boolean isBusinessSuccess(String body) {
        jsonObject = JSON.parseObject(body);
        return jsonObject.getInteger("code") == 0;
    }


    @Override
    public String getBusinessData(String body) {
        return jsonObject.getString("data");
    }
}
