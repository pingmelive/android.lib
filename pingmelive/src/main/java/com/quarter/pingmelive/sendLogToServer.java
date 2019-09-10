package com.quarter.pingmelive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class sendLogToServer extends Service {


    ArrayList<pingModel> list;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("Service","Service");
        Log.e("Service","Local service started");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(("localtoServerSync".hashCode()),getServiceNotification());
        }
//        if(internetIsConnected())
//        {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
            list = dbHelper.getPendingEvents();
            if(list.size()>0)
            {
                for(int i = 0 ; i < list.size() ; i++) {
                    sync_local_to_server(list.get(i), getApplicationContext());
                }
            }
            else
            {
                sendBroadcast(new Intent("update_sync_time"));
            }

            stopSelf();
        Log.e("Service","Local service stopped");


    }

    public void sync_local_to_server(pingModel sync_modal, Context context)
    {
        Log.e("Data","Sent to server");
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
