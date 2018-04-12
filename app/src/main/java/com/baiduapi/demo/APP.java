package com.baiduapi.demo;

import android.app.Application;
import android.content.Context;

/**
 * @author Android(JiaWei)
 * @date 2018/4/12.
 */

public class APP extends Application {
    private static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }

    public static Context getContext() {
        return applicationContext;
    }
}
