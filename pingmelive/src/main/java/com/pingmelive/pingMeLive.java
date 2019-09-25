package com.pingmelive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;


public final class pingMeLive {

    private final static String TAG = "pingMeLive";

    //General constants
    private static final String CAOC_HANDLER_PACKAGE_NAME = "com.pingmelive.pingMeLive";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";

    private static final String SHARED_PREFERENCES_FIELD_TIMESTAMP = "LAST_CRASH_TIME_STAMP";


    @SuppressLint("StaticFieldLeak") //This is an application-wide component
    private static Application application;

    private static DBHelper dbHelper;
    private static pingMePref pingMePref;

    protected final Builder builder;


    @SuppressLint("InflateParams")
    protected pingMeLive(Builder builder) {
        this.builder = builder;
    }

    public static void install(@Nullable final Context context,boolean errorEvents, final String errorEventTitle, String API_KEY,String PROJECT_ID) {

        if(errorEvents) {
            try {
                if (context == null) {
                    Log.e(TAG, "Install failed: context is null!");
                } else {


                    if (errorEventTitle == null || errorEventTitle.trim().length() <= 0) {
                        Log.e(TAG, "errorEventTitle needed check your application class");
                        Log.e(TAG, "pingMeLive not installed.");
                        return;
                    }

                    if (API_KEY == null || API_KEY.trim().length() <= 0) {
                        Log.e(TAG, "API KEY needed check your application class");
                        Log.e(TAG, "pingMeLive not installed.");
                        return;
                    }

                    if (PROJECT_ID == null || PROJECT_ID.trim().length() <= 0) {
                        Log.e(TAG, "PROJECT_ID needed check your application class");
                        Log.e(TAG, "pingMeLive not installed.");
                        return;
                    }

                    dbHelper = DBHelper.getInstance(context);
                    pingMePref = com.pingmelive.pingMePref.getInstance(context);

                    pingMePref.setAPIKey(API_KEY);
                    pingMePref.setAppId(PROJECT_ID);

                    //INSTALL!
                    final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

                    if (oldHandler != null && oldHandler.getClass().getName().startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                        Log.e(TAG, "pingMeLive was already installed, doing nothing!");
                    } else {
                        if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                            Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use a custom UncaughtExceptionHandler, you must initialize it AFTER pingMeLive! Installing anyway, but your original handler will not be called.");
                        }

                        application = (Application) context.getApplicationContext();

                        //We define a default exception handler that does what we want so it can be called from Crashlytics/ACRA
                        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread thread, final Throwable throwable) {

                                Log.e(TAG, "App has crashed, executing pingMeLive's UncaughtExceptionHandler", throwable);


                                if (hasCrashedInTheLastSeconds(application)) {
                                    Log.e(TAG, "App already crashed recently, not starting custom error activity because we could enter a restart loop. Are you sure that your app does not crash directly on init?", throwable);
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable);
                                        return;
                                    }
                                } else {

                                    setLastCrashTimestamp(application, new Date().getTime());

                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    throwable.printStackTrace(pw);
                                    String stackTraceString = sw.toString();
                                    detailedEvent(errorEventTitle, throwable.getMessage(), stackTraceString);

                                }

                                killCurrentProcess();

                            }
                        });
                    }

                    Log.i(TAG, "pingMeLive has been installed.");
                    dbHelper.sendData();
                }
            } catch (Throwable t) {
                Log.e(TAG, "An unknown error occurred while installing pingMeLive, it may not have been properly initialized. Please report this as a bug if needed.", t);
            }
        }
        else {

            if (API_KEY == null || API_KEY.trim().length() <= 0) {
                Log.e(TAG, "API KEY needed check your application class");
                Log.e(TAG, "pingMeLive not installed.");
                return;
            }

            if (PROJECT_ID == null || PROJECT_ID.trim().length() <= 0) {
                Log.e(TAG, "appId needed check your application class");
                Log.e(TAG, "pingMeLive not installed.");
                return;
            }


            Log.i(TAG, "pingMeLive has been installed, But without error events, to activate that setErrorEventEnabled(true) in your application class.");

            dbHelper = DBHelper.getInstance(context);
            pingMePref = com.pingmelive.pingMePref.getInstance(context);

            pingMePref.setAPIKey(API_KEY);
            pingMePref.setAppId(PROJECT_ID);

            dbHelper.sendData();
        }
    }


    /**
     * INTERNAL method that kills the current process.
     * It is used after restarting or killing the app.
     */
    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    /**
     * INTERNAL method that stores the last crash timestamp
     *
     * @param timestamp The current timestamp.
     */
    @SuppressLint("ApplySharedPref") //This must be done immediately since we are killing the app
    private static void setLastCrashTimestamp(@NonNull Context context, long timestamp) {
        pingMePref.updateLongValue(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp);
    }

    /**
     * INTERNAL method that gets the last crash timestamp
     *
     * @return The last crash timestamp, or -1 if not set.
     */
    private static long getLastCrashTimestamp(@NonNull Context context) {
        return pingMePref.getLongvalue(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1);
    }

    /**
     * INTERNAL method that tells if the app has crashed in the last seconds.
     * This is used to avoid restart loops.
     *
     * @return true if the app has crashed in the last seconds, false otherwise.
     */
    private static boolean hasCrashedInTheLastSeconds(@NonNull Context context) {
        long lastTimestamp = getLastCrashTimestamp(context);
        long currentTimestamp = new Date().getTime();

        int minTimeBetweenCrashesMs = 2000;
        return (lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < minTimeBetweenCrashesMs);
    }

    public static void simpleEvent(String groupTitle,String message)
    {
        try {
            if (dbHelper != null) {

                pingModel pingModel = new pingModel();
                pingModel.setGroupTitle(groupTitle);
                pingModel.setMessage(dbHelper.getMessage(message));
                dbHelper.addEvent(pingModel);

            }
        }
        catch (Exception ignored)
        {

        }
    }

    public static void detailedEvent(String groupTitle,String message,String detailedText)
    {
        try {
            if (dbHelper != null) {

                pingModel pingModel = new pingModel();
                pingModel.setGroupTitle(groupTitle);
                pingModel.setMessage(dbHelper.getMessage(message));
                pingModel.setDetailText(dbHelper.getDetailedText(detailedText));
                dbHelper.addEvent(pingModel);

            }
        }
        catch (Exception ignored)
        {

        }
    }


    public static class Builder {

        boolean ErrorEventEnabled = true;
        String ErrorEventTitle = null;
        String API_KEY = null;
        String PROJECT_ID = null;
        Context context;

        public Builder(Context context)
        {
            this.context = context;
        }

        boolean isErrorEventEnabled() {
            return ErrorEventEnabled;
        }

        public Builder setErrorEventEnabled(boolean setErrorEventEnabled) {
            this.ErrorEventEnabled = setErrorEventEnabled;
            return this;
        }

        String getErrorEventTitle() {
            return ErrorEventTitle;
        }

        public Builder setErrorEventTitle(String setErrorEventTitle) {
            this.ErrorEventTitle = setErrorEventTitle;
            return this;
        }

        String getAPI_KEY() {
            return API_KEY;
        }

        public Builder setAPI_KEY(String API_KEY) {
            this.API_KEY = API_KEY;
            return this;
        }

        public String getPROJECT_ID() {
            return PROJECT_ID;
        }

        public Builder setPROJECT_ID(String PROJECT_ID) {
            this.PROJECT_ID = PROJECT_ID;
            return this;
        }

        public void install() {
            pingMeLive.install(context,isErrorEventEnabled(),getErrorEventTitle(),getAPI_KEY(),getPROJECT_ID());
        }
    }
}
