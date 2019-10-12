package com.pingmelive.lib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class sendLogToServer extends Service {

    ArrayList<pingModel> list;
    DBHelper dbHelper;
    pingMePref pingMePref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("Service","Service");
        Log.e("Service","Local service started");


        dbHelper = DBHelper.getInstance(getApplicationContext());
        pingMePref = com.pingmelive.lib.pingMePref.getInstance(getApplicationContext());

        if(pingMePref.getAPIKey()==null)
        {
            pingMePref.log("PingMeLive not installed");
            pingMePref.log("API KEY Not Found, Check your application class for a valid API KEY!");
            stopSelf();
            return;
        }

        if(pingMePref.getAppId()==null)
        {
            pingMePref.log("PingMeLive not installed");
            pingMePref.log("APP ID Not Found, Check your application class for a valid APP ID!");
            stopSelf();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(("localtoServerSync".hashCode()),getServiceNotification());
        }

            list = dbHelper.getPendingEvents();
            if(list.size()>0)
            {
                Log.e("pingMeLive","Sending "+list.size()+" pings");
                for(int i = 0 ; i < list.size() ; i++) {
                    sync_local_to_server(list.get(i));
                }
            }
            else
            {
                Log.e("pingMeLive","No pings to send");
                sendBroadcast(new Intent("update_sync_time"));
            }

            stopSelf();


    }

    public void sync_local_to_server(final pingModel pingModel) {

        //Log.e("pingMeLive","Sending to server "+pingModel.getMessage());

        String url = "https://pingmelive.com/event/push/";

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    //Log.e("API Response",""+response);
                    Log.e("Data","Sent to server - "+pingModel.getGroupTitle());
                    dbHelper.removeEvent(pingModel.getId());

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error!=null && error.getMessage()!=null) {
                        //Log.e("TAG", "Error sending the log " + error.getMessage());
                    }
                    else
                    {
                        //Log.e("TAG", "Error sending the log");
                    }
                }
            }) {

                @Override
                public byte[] getBody() throws AuthFailureError {

                    JSONObject jsonObject = new JSONObject();
                    try {

                        jsonObject.put("groupTitle", pingModel.getGroupTitle());
                        jsonObject.put("message", pingModel.getMessage());
                        jsonObject.put("detailedText", pingModel.getDetailText());
                        jsonObject.put("eventDateTime", pingModel.getEventDateTime());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String str = jsonObject.toString();

                    //Log.e("API Body",jsonObject.toString());
                    return str.getBytes();
                }

                ;

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  headers = new HashMap<>();
                    headers.put("Content-Type","application/json");
                    headers.put("apikey",pingMePref.getAPIKey());
                    headers.put("projectid",pingMePref.getAppId());

                    //Log.e("API Header",headers.toString());
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(strReq);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service","Local service stopped");
    }

    public Notification getServiceNotification()
    {

        return new NotificationCompat.Builder(getApplicationContext(),getSilentChannelID())
                .setContentTitle("Refreshing")
                .setContentText("Please wait...")
                .setProgress(100,0,true)
                .setChannelId(getSilentChannelID())
                .build();
    }

    public String getSilentChannelID()
    {
        createSilentchannel();
        String channel_name="Silent".toUpperCase();
        return channel_name+""+channel_name.hashCode();
    }

    public void createSilentchannel()
    {
        String channel_name="Silent".toUpperCase();
        String channel_id=channel_name+""+channel_name.hashCode();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // The user-visible name of the channel.

            // The user-visible description of the channel.
            String description = channel_name+" Notifications";

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            // create android channel
            NotificationChannel androidChannel = new NotificationChannel(channel_id,
                    channel_name, NotificationManager.IMPORTANCE_LOW);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);

            androidChannel.setSound(getSilenceSound(),audioAttributes);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(false);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.BLUE);

            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // setDescription
            androidChannel.setDescription(description);
            // set badges
            androidChannel.setShowBadge(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(androidChannel);
            }
        }
    }

    public Uri getSilenceSound()
    {
        return Uri.parse("android.resource://com.billse/" + R.raw.silence);
    }

}
