package com.mr21yse.application;


import android.app.Application;

public class numchangerActivity extends Application {

    private String zerocount = "1000";//左　TGTcontの初期値設定

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String getZerocount() {
        return zerocount;
    }

    public void setZerocount(String str) {
        zerocount = str;
    }
}