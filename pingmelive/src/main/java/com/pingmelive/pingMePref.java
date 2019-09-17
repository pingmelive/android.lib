package com.pingmelive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class pingMePref {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefeditor;

    public Context context;

    public static volatile pingMePref pingMePref;


    public static pingMePref getInstance(Context context) {

        if (pingMePref == null) { //Check for the first time

            synchronized (pingMePref.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (pingMePref == null) pingMePref =new pingMePref(context);
            }
        }

        return pingMePref;
    }

    public pingMePref(Context context)
    {
        this.context=context;
        this.sharedPreferences = this.context.getSharedPreferences("pingMeLivePref", 0);
        this.prefeditor = sharedPreferences.edit();
    }

    private void updateBooleanvalue(String valuename, Boolean newvalue) {
        prefeditor.putBoolean(valuename, newvalue).apply();
    }

    private Boolean getBooleanvalue(String valuename, Boolean defaultvalue) {
        return sharedPreferences.getBoolean(valuename, defaultvalue);
    }

    public boolean isMute(String title)
    {
        return getBooleanvalue(""+title.hashCode(),false);
    }

    public void MuteNotifications(String title, Boolean boolValue)
    {
        updateBooleanvalue(""+title.hashCode(),boolValue);
    }

    public boolean isWebHookEnabled(String title)
    {
        return getBooleanvalue(""+title.hashCode()+"_webhook",false);
    }

    public void webHookEnable(String title, Boolean boolValue)
    {
        updateBooleanvalue(""+title.hashCode()+"_webhook",boolValue);
    }

    public boolean isEmailEnabled(String title)
    {
        return getBooleanvalue(""+title.hashCode()+"_email",false);
    }

    public void emailEnable(String title, Boolean boolValue)
    {
        updateBooleanvalue(""+title.hashCode()+"_email",boolValue);
    }

    public String getWebHookUrl(String title)
    {

        return getStringValue(""+title.hashCode()+"_webhook_url",null);
    }

    public void setWebHookUrl(String title, String url)
    {
        updateStringvalue(""+title.hashCode()+"_webhook_url",url);
    }

    public String getEmail(String title)
    {
        return getStringValue(""+title.hashCode()+"_email_id",null);
    }

    public void setEmail(String title, String email)
    {
        updateStringvalue(""+title.hashCode()+"_email_id",email);
    }

    public void updateStringvalue(String valuename, String newvalue) {
        prefeditor.putString(valuename, newvalue).apply();
    }

    private String getStringValue(String valuename, String def) {
        return sharedPreferences.getString(valuename, def);
    }

    public void log(String logstring) {
        Log.e(context.getClass().getSimpleName(), "" + logstring);
    }

    public void toast(String message)
    {
        try {
            Toast toast = Toast.makeText(context,message, Toast.LENGTH_LONG);
            View view = toast.getView();

            //Gets the actual oval background of the Toast then sets the colour filter
            view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

            //Gets the TextView from the Toast so it can be editted
            TextView text = view.findViewById(android.R.id.message);
            text.setTextColor(Color.WHITE);

            toast.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void setAPIKey(String apiKey)
    {
        if(apiKey!=null) {
            updateStringvalue("APIKEY", apiKey);
        }
        else
        {
            Log.e("pingMeLive APIKEY","Cannot be null!!");
        }
    }

    public String getAPIKey()
    {
        return getStringValue("APIKEY",null);
    }

    public void setAppId(String appId)
    {
        if(appId!=null) {
            updateStringvalue("APPID", appId);
        }
        else
        {
            Log.e("pingMeLive APPID","Cannot be null!!");
        }
    }

    public String getAppId()
    {
        return getStringValue("APPID",null);
    }

}
