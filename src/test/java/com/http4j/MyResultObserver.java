package com.http4j;

public class MyResultObserver implements ResultObserver {


    @Override
    public void callHttpStart() {
        System.out.println("=====parent callHttpStart");
    }
}
