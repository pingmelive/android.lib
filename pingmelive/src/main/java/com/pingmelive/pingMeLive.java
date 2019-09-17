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

import android.util.Log;

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
    private static int minTimeBetweenCrashesMs = 2000;

    //Extras passed to the error activity
    private static final String EXTRA_CONFIG = "cat.ereza.customactivityoncrash.EXTRA_CONFIG";
    private static final String EXTRA_STACK_TRACE = "cat.ereza.customactivityoncrash.EXTRA_STACK_TRACE";
    private static final String EXTRA_ACTIVITY_LOG = "cat.ereza.customactivityoncrash.EXTRA_ACTIVITY_LOG";

    //General constants
    private static final String INTENT_ACTION_ERROR_ACTIVITY = "cat.ereza.customactivityoncrash.ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "cat.ereza.customactivityoncrash.RESTART";
    private static final String CAOC_HANDLER_PACKAGE_NAME = "cat.ereza.customactivityoncrash";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";
    private static final int MAX_STACK_TRACE_SIZE = 131071; //128 KB - 1
    private static final int MAX_ACTIVITIES_IN_LOG = 50;

    //Shared preferences
    private static final String SHARED_PREFERENCES_FILE = "custom_activity_on_crash";
    private static final String SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp";

    //Internal variables
    @SuppressLint("StaticFieldLeak") //This is an application-wide component
    private static Application application;
    //private static pingMeConfig config = new pingMeConfig();
    private static final Deque<String> activityLog = new ArrayDeque<>(MAX_ACTIVITIES_IN_LOG);
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);
    private static boolean isInBackground = true;

    private static DBHelper dbHelper;
    private static pingMePref pingMePref;

    public static void install(@Nullable final Context context, final String errorGroupTitle, String APIKEY,String appId) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!");
            } else {


                if(errorGroupTitle==null || errorGroupTitle.trim().length()<=0)
                {
                    Log.e(TAG, "errorGroupTitle needed check your application class");
                    Log.e(TAG, "pingMeLive not installed.");
                    return;
                }

                if(APIKEY==null || APIKEY.trim().length()<=0)
                {
                    Log.e(TAG, "API KEY needed check your application class");
                    Log.e(TAG, "pingMeLive not installed.");
                    return;
                }

                if(appId==null || appId.trim().length()<=0)
                {
                    Log.e(TAG, "appId needed check your application class");
                    Log.e(TAG, "pingMeLive not installed.");
                    return;
                }

                dbHelper = DBHelper.getInstance(context);
                pingMePref = com.pingmelive.pingMePref.getInstance(context);

                pingMePref.setAPIKey(APIKEY);
                pingMePref.setAppId(appId);

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
                                    detailedEvent(errorGroupTitle,throwable.getMessage(),stackTraceString);

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
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp).commit();
    }

    /**
     * INTERNAL method that gets the last crash timestamp
     *
     * @return The last crash timestamp, or -1 if not set.
     */
    private static long getLastCrashTimestamp(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1);
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
}
