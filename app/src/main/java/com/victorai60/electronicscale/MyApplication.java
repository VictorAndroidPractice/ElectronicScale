package com.victorai60.electronicscale;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    public static Context application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}
