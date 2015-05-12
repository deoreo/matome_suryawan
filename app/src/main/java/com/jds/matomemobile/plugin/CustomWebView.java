package com.jds.matomemobile.plugin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.jds.matomemobile.util.ScrollAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by JDS on 3/20/15.
 */
public class CustomWebView {

    private FrameLayout frameContainer;
    private WebView webView;
    private CustomWebViewClient customClient;
    private CustomChromeClient customChromeClient;
    private Context context;
    private View rootView;
    private int rWebViewId;
    private int rFrameContainerId;
    private int reloaded;
    private Stack<String> histories;
    private List<Integer> animQueue;
    private boolean isResume;
    private boolean stillAnimate;

    public CustomWebView(Context context, View view, int rWebViewId, int rFrameContainerId, boolean is_resume) {
        this.context = context;
        this.rootView = view;
        this.rWebViewId = rWebViewId;
        this.rFrameContainerId = rFrameContainerId;
        histories = new Stack<>();
        animQueue = new ArrayList<>();
        frameContainer = (FrameLayout) rootView.findViewById(rFrameContainerId);
//        webView = (WebView) rootView.findViewById(rWebViewId);
        webView = new WebView(context);
        frameContainer.addView(webView);
        resetWebView();
        Activity activity = (Activity) context;
        customClient = new CustomWebViewClient(this, activity, is_resume, view, rWebViewId);
        stillAnimate = false;
    }

    public void setCustomChromeClient(ProgressBar progressBar, int rAnimId) {
        customChromeClient = new CustomChromeClient(context, progressBar, rAnimId);
    }

    public void useCustomClient(boolean isCustom) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(false);

        if (isCustom) {
            webView.setWebViewClient(customClient);
            webView.setWebChromeClient(customChromeClient);
        }
    }

    public int getScrollYPosition() {
        return webView.getScrollY();
    }

    public void setScrollYPosition(int value) {
        webView.setScrollY(value);
    }

    public void setOnTouchListener(View.OnTouchListener touchListener) {
        webView.setOnTouchListener(touchListener);
    }

    public void loadUrl(String url) {
        resetWebView();
        webView.loadUrl(url);
    }

    public void reload() {
        resetWebView();
        customClient.setReload(true);
        webView.loadUrl(webView.getUrl());
    }

    public void requestFocus() {
        webView.requestFocus();
    }

    private void addHistories(String url) {
        if (histories.size() > 0) {
            if (!histories.lastElement().equals(url)) {
                histories.push(url);
            }
        } else {
            histories.push(url);
        }
    }

    private void resetWebView() {
//        if(reloaded == 0) {
        webView.clearCache(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        webView.getSettings().setAppCacheMaxSize(0);
        if (Arrays.binarySearch(context.databaseList(), "webview.db") >= 0) {
            context.deleteDatabase("webview.db");
        }
        if (Arrays.binarySearch(context.databaseList(), "webviewCache.db") >= 0) {
            context.deleteDatabase("webviewCache.db");
        }
//        } else {
//            reloaded++;
//            if(reloaded == 3) reloaded = 0;
//        }
    }

    public Context getContext() {
        return webView.getContext();
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return webView.getLayoutParams();
    }

    public WebView getWebView() {
        return webView;
    }

    public int getProgress() {
        return webView.getProgress();
    }

    public void setDrawingCacheEnabled(boolean b) {
        webView.setDrawingCacheEnabled(b);
    }

    public Bitmap getDrawingCache() {
        return webView.getDrawingCache();
    }

    public String getUrl() {
        return webView.getUrl();
    }

    public boolean getLastUrlFail() {
        return customClient.getLastUrlFail();
    }

    public boolean canGoBack() {
        return customClient.getBrowseHistories().size() > 1 ? true : false;
//        return webView.canGoBack();
    }

    public void goBack() {
        loadUrl(customClient.getBackUrl());
    }

    public boolean isFileLoadedFromUrl() {
        return customClient.isFileLoadFromUrl();
    }

    public void scrollToZero() {
        webView.clearAnimation();
        ScrollAnimation anim = new ScrollAnimation(webView, webView.getScrollX(), 0);
        anim.setDuration(5);
        webView.setAnimation(anim);
        anim.start();
    }

    public void kill() {
        webView.removeAllViews();
        webView.clearHistory();
        webView.clearCache(true);
        webView.pauseTimers(); //new code
        webView.destroy();
        webView = null;
    }

    public void loadBrowserHistories() {
        customClient.loadBrowseHistories();
    }

    public void saveState(Bundle outState) {
        webView.saveState(outState);
    }

    public void restoreState(Bundle inState) {
        webView.restoreState(inState);
    }

    public boolean isResume() {
        return isResume;
    }

    public void setResume(boolean isResume) {
        customClient.setResume(isResume);
        this.isResume = isResume;
    }

    public void stopLoading() {
        try {
            webView.stopLoading();
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }
    }

    public void onPause() {
        try {
            webView.onPause();
        } catch (Exception e) {

        }

    }

    public void pauseTimers() {
        try {
            webView.pauseTimers();
        } catch (Exception e) {

        }
    }

    public void destroy() {
        try {
            webView.destroy();
        } catch (Exception e) {

        }
    }

    public void setVisibility(final int visible) {
        if (stillAnimate == false) {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(500);
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            if (visible == View.GONE || visible == View.INVISIBLE) {
                frameContainer.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        stillAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        frameContainer.setVisibility(visible);
                        stillAnimate = false;
                        runNextAnim();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else if (visible == View.VISIBLE) {
                frameContainer.setVisibility(visible);
                frameContainer.startAnimation(fadeIn);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        stillAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        stillAnimate = false;
                        runNextAnim();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        } else {
            if (animQueue.size() < 5) {
                animQueue.add(visible);
            } else {
                animQueue.add(View.VISIBLE);
            }
        }
    }

    private void runNextAnim() {
        if (animQueue.size() > 0) {
            setVisibility(animQueue.get(0));
            List<Integer> temp = new ArrayList<>();
            for (int i = 1; i < animQueue.size(); i++) {
                temp.set(i - 1, animQueue.get(i));
            }
            animQueue = temp;
        } else {
            animQueue.clear();
        }
    }

    public void removeAllViews() {
        webView.removeAllViews();
    }

    public void requestLayout() {
        webView.requestLayout();
    }
}
