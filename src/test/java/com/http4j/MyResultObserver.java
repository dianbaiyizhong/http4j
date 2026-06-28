package com.http4j;

public class MyResultObserver extends ResultObserver {


    @Override
    public void callHttpStart() {
        System.out.println("=====parent callHttpStart");
    }


    @Override
    public void callBusinessSuccess() {
        super.callBusinessSuccess();
    }


    @Override
    public void callBusinessFail(String message) {
        super.callBusinessFail(message);
    }
}
