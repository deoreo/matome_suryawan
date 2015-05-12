package com.jds.matomemobile.plugin;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.jds.matomemobile.BrowserFragment;
import com.jds.matomemobile.ManagerActivity;

/**
 * Created by JDS on 3/20/15.
 */
public class CustomChromeClient extends WebChromeClient {

    WebData webData = new WebData();
    CustomWebView customWebView;
    private ProgressBar progressBar;
    private Context context;
    private int animId;

    public CustomChromeClient(Context context, ProgressBar progressBar, int animId) {
        this.context = context;
        this.progressBar = progressBar;
        this.animId = animId;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if(newProgress <= 100) {
            webData.setProgress(newProgress);
        }

        super.onProgressChanged(view, newProgress);
        if(newProgress < 100) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(newProgress);
        } else {
            progressBar.setProgress(newProgress);
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(500);
            progressBar.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }
}
