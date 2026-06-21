package com.http4j;

import com.alibaba.fastjson2.JSONObject;

public class MyTest {
    public static void main(String[] args) {

        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new MyResultObserver());
        cfg.setDefaultRule(new MyRule());
        Http4jConfig.setDefaultConfig(cfg);


        MyResult myResult = Http4j.request("http://localhost:5000/api/v1/ready")
                .setRule(new MyRule())
                .setObserver(new MyResultObserver())
                .rule(new ResultRule() {
                    @Override
                    public boolean isBusinessSuccess(String body) {
                        return true;
                    }
                })
                .observe(new MyResultObserver() {
                    @Override
                    public void callHttpStart() {
                        System.out.println("========");
                    }
                })
                .execute(MyResult.class);


        System.out.println(myResult);
    }
}
