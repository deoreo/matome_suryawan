package com.jds.matomemobile.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by windows 7 on 05/03/2015.
 */
public class ResizeAnimation extends Animation {
    private int mWidth, mHeight, mStartWidth, mStartHeight;
    private View mView;

    public ResizeAnimation(View view, int width, int height) {
        mView = view;
        mWidth = width;
        mStartWidth = view.getWidth();
        mHeight = height;
        mStartHeight = view.getHeight();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
        int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);

        mView.getLayoutParams().width = newWidth;
        mView.getLayoutParams().height = newHeight;

        mView.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public void setFromHeight(int fromHeight) {
        this.mStartHeight = fromHeight;
    }
}
