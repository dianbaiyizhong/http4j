package com.http4j;

public class MyResultObserver extends ResultObserver {


    @Override
    public void callHttpStart() {
        System.out.println("=====parent callHttpStart");
    }




}
