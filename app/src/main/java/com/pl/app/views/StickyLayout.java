package com.pl.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import java.util.NoSuchElementException;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 singwhatiwanna
 * https://github.com/singwhatiwanna
 * Modified by Alex PL on 2016/7/18.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
public class StickyLayout extends LinearLayout{
    private Context mContext;
    private View mHeader;
    private View mContent;
    private OnQuitTouchEventListener mQuitTouchEventListener;

    //header height, unit:px
    private int mOriginalHeaderHeight;
    private int mHeaderHeight;

    public final static int STATUS_EXPAND = 1;
    public final static int STATUS_COLLAPSED = 2;
    private int mStatus = STATUS_EXPAND;
    private int mTouchSlop;

    private boolean mIsSticky = true;
    private boolean mInitDataSucceed = false;
    private boolean mDisallowInterceptTouchEventOnHeader = true;

    //private int mLastX = 0;
    private int mLastY = 0;
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    public StickyLayout(Context context) {
        super(context);
        mContext = context;
    }

    public StickyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public StickyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && (mHeader == null || mContent == null))
            initData();
    }

    private void initData() {
        int headerId = getResources().getIdentifier("sticky_header", "id", mContext.getPackageName());
        int contentId = getResources().getIdentifier("sticky_content", "id", mContext.getPackageName());
        if (headerId != 0 && contentId != 0) {
            mHeader = findViewById(headerId);
            mContent = findViewById(contentId);
            mOriginalHeaderHeight = mHeader.getMeasuredHeight();
            mHeaderHeight = mOriginalHeaderHeight;
            mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            if (mHeaderHeight > 0)
                mInitDataSucceed = true;
        } else {
            throw new NoSuchElementException("Did your view with id \\\"sticky_header\\\" or \\\"sticky_content\\\" exists?");
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int intercept = 0;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //mLastX = x;
                mLastY = y;
                mLastXIntercept = x;
                mLastYIntercept = y;
                intercept = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (mDisallowInterceptTouchEventOnHeader && y < mHeaderHeight)
                    intercept = 0;
                else if (Math.abs(deltaX) >= Math.abs(deltaY))
                    intercept = 0;
                else if (mStatus == STATUS_EXPAND && deltaY <= -mTouchSlop)
                    intercept = 1;
                else if (mQuitTouchEventListener != null && mQuitTouchEventListener.quitTouchEvent(ev) && deltaY >= mTouchSlop)
                    intercept = 1;

                break;

            case MotionEvent.ACTION_UP:
                intercept = 0;
                mLastXIntercept = mLastYIntercept = 0;
                break;
        }

        return intercept != 0 && mIsSticky;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsSticky)
            return true;

        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - mLastY;
                mHeaderHeight += deltaY;
                setHeaderHeight(mHeaderHeight);
                break;
            case MotionEvent.ACTION_UP:
                int destHeight;
                if (mHeaderHeight < mOriginalHeaderHeight << 1) {
                    destHeight = 0;
                    mStatus = STATUS_COLLAPSED;
                } else {
                    destHeight = mOriginalHeaderHeight;
                    mStatus = STATUS_EXPAND;
                }
                smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void smoothSetHeaderHeight(final int from, final int to, long duration) {
        final int frameCount = (int) (duration / 1000f * 30) + 1;
        final float partition = (to - from) / frameCount;
        new Thread("Thread#smoothSetHeaderHeight") {
            @Override
            public void run() {
                for (int i = 0; i < frameCount; i++) {
                    final int height;
                    if (i == frameCount - 1)
                        height = to;
                    else
                        height = (int) (from + partition * i);

                    post(new Runnable() {
                        @Override
                        public void run() {
                            setHeaderHeight(height);
                        }
                    });
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void setHeaderHeight(int height) {
        if (!mInitDataSucceed)
            initData();

        if (height <= 0)
            height = 0;
        if (height > mOriginalHeaderHeight)
            height = mOriginalHeaderHeight;

        if (height == 0)
            mStatus = STATUS_COLLAPSED;
        else
            mStatus = STATUS_EXPAND;

        if (mHeader != null && mHeader.getLayoutParams() != null) {
            mHeader.getLayoutParams().height = height;
            mHeader.requestLayout();
            mHeaderHeight = height;
        }
    }

    public void requestDisallowInterceptTouchEventOnHeader(boolean disallowIntercept) {
        mDisallowInterceptTouchEventOnHeader = disallowIntercept;
    }

    public void setOnQuitTouchEventListener(OnQuitTouchEventListener l) {
        mQuitTouchEventListener = l;
    }

    public interface OnQuitTouchEventListener {
        boolean quitTouchEvent(MotionEvent event);
    }
}
