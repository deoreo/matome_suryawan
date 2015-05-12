package com.jds.matomemobile.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.webkit.WebView;

/**
 * Created by windows 7 on 05/03/2015.
 */
public class ScrollAnimation extends Animation {
    private int mScrollX, mStartScrollX, mScrollY, mStartScrollY;
    private WebView mView;

    public ScrollAnimation(WebView view, int scrollX, int scrollY) {
        mView = view;
        mScrollX = scrollX;
        mStartScrollX = view.getScrollX();
        mScrollY = scrollY;
        mStartScrollY = view.getScrollY();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newScrollX = mStartScrollX + (int) ((mScrollX - mStartScrollX) * interpolatedTime);
        int newScrollY = mStartScrollY + (int) ((mScrollY - mStartScrollY) * interpolatedTime);

        mView.setScrollX(newScrollX);
        mView.setScrollY(newScrollY);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
