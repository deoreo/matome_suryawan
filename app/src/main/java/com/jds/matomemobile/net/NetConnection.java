package com.jds.matomemobile.net;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by windows 7 on 13/03/2015.
 */
public class NetConnection extends TimerTask {
    private Activity activity;
    private Timer timer;
    private WebView view;
//    private BrowseLocal browseLocal;

//    public NetConnection(Activity activity, int duration, WebView view) {
//        this.activity = activity;
//        this.view = view;
//        timer = new Timer();
//        timer.schedule(this, duration, duration);
//        browseLocal = new BrowseLocal(activity.getFilesDir().getPath() + "/cache");
//    }

    public boolean isOnline(ConnectivityManager cm) {
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    public void run() {
//        try {
//            if (activity == null || activity.isFinishing()) {
//                // Activity killed
//                this.cancel();
//                return;
//            }
//
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (isOnline()) {
//                        browseLocal.saveLocal(view, "cache" + WebData.WEB_ARCHIVE_EXT);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
