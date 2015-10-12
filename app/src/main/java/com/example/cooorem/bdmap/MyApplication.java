package com.example.cooorem.bdmap;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2015/10/8.
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context=getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
