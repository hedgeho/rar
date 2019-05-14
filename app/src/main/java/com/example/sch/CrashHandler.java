package com.example.sch;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

public class CrashHandler extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                SharedPreferences pref = getSharedPreferences("pref", 0);
                pref.edit().putString("error", t.toString() + ": " + e.toString()).apply();
            }
        });
    }
}
