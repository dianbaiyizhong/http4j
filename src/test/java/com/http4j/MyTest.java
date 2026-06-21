package com.http4j;

import com.alibaba.fastjson2.JSONObject;

public class MyTest {
    public static void main(String[] args) {

        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new MyResultObserver());
        cfg.setDefaultRule(new MyRule());
        cfg.setBaseUrl("http://localhost:8000");
//        Http4jConfig.setDefaultConfig(cfg);


        String myResult = Http4j.request("/api/users")
                .setBaseUrl("http://localhost:8000")
                .execute();

        System.out.println(myResult);
    }
}
