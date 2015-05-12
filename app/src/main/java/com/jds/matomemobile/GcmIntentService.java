package com.jds.matomemobile;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jds.matomemobile.plugin.DBHelper;

import java.io.IOException;
import java.sql.SQLException;

public class GcmIntentService extends IntentService {

    private int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if(!extras.isEmpty()) {

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
                sendNotification(extras.toString(), extras.toString(), extras.toString(), extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
                sendNotification(extras.toString(), extras.toString(), extras.toString(), extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
                for (int i=0; i<5; i++){
                    Log.i(String.valueOf(R.string.TAG), "Working... " + (i+1)
                                + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.d("catch", e.getMessage());
                    }
                }

                Log.i(String.valueOf(R.string.TAG), "Completed work @ " + SystemClock.elapsedRealtime());

                sendNotification(extras.getString("title"), extras.getString("content"), extras.getString("link"), extras.getString("thumbnail"));
                Log.i(String.valueOf(R.string.TAG), "Received: " + extras.toString());
            }

        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification (String title, String content, String link, String thumbnail) {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, SplashActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.matome_notification_icon)
                .setContentTitle("Matome")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Matome"))
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setTicker("New Story from Matome")
                .setAutoCancel(true)
                .setSubText(title);

        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);

        switch (am.getRingerMode())
        {
            case AudioManager.RINGER_MODE_VIBRATE:
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                break;
            default:
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        NOTIFICATION_ID = NOTIFICATION_ID + 1;

        StoreToLocalDatabase(title, content, link, thumbnail);
    }

    private void StoreToLocalDatabase(String title, String content, String link, String thumbnail){
        //send to database
        DBHelper dbHelper = new DBHelper(this);

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("link", link);
        values.put("thumbnail", thumbnail);
        values.put("status", 0);

        try {

            dbHelper.createDatabase();
            dbHelper.openDatabase();

            dbHelper.insertData("storage", values);

            int count = dbHelper.countData();
            Log.d("database total: ", String.valueOf(count));

            dbHelper.closeDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}