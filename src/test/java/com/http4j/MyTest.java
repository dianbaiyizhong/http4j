package com.http4j;

public class MyTest {
    public static void main(String[] args) {

        Http4jConfig cfg = new Http4jConfig();
        cfg.setDefaultObserver(new MyResultObserver());
        cfg.setDefaultRule(new MyRule());
        Http4jConfig.setDefaultConfig(cfg);


        MyResult myResult = Http4j.request("http://localhost:5000/api/v1/ready")
                .setDefaultRule(new MyRule())
                .observe(new MyResultObserver() {
//                    @Override
//                    public void callHttpStart() {
//                        System.out.println("========");
//                    }
                })
                .execute(MyResult.class);


        System.out.println(myResult);
    }
}
