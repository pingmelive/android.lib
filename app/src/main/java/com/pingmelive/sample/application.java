package com.pingmelive.sample;

import android.app.Application;

import com.pingmelive.lib.pingMeLive;

public class application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new pingMeLive.Builder(getApplicationContext())
                .setErrorEventEnabled(true)
                .setErrorEventTitle("ERROR_TITLE")
                .setAPI_KEY("YOUR_API_KEY")
                .setPROJECT_ID("YOUR_APP_ID")
                .install();
    }
}
