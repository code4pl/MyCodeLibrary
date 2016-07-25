package com.pl.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;

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
public class PinnedHeaderExpandableListView extends ExpandableListView implements OnScrollListener{
    private View mTouchTarget;
    private View mHeaderView;
    private int mHeaderHeight;
    private int mHeaderWidth;
    private OnScrollListener mScrollListener;
    private OnHeaderUpdateListener mHeaderUpdateListener;

    private boolean mActionDownHappened = false;
    private boolean mIsHeaderGroupClickable = true;

    public PinnedHeaderExpandableListView(Context context) {
        super(context);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setFadingEdgeLength(0);
        setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if (l != this)
            mScrollListener = l;
        else
            mScrollListener = null;
        super.setOnScrollListener(l);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null)
            mScrollListener.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 0)
            refreshHeader();
        if (mScrollListener != null)
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView == null)
            return;
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
        mHeaderWidth = mHeaderView.getMeasuredWidth();
        mHeaderHeight = mHeaderView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeaderView == null)
            return;
        int delta = mHeaderView.getTop();
        mHeaderView.layout(0, delta, mHeaderWidth, mHeaderHeight+delta);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderView != null)
            drawChild(canvas, mHeaderView, getDrawingTime());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        int item = pointToPosition(x, y);
        if (mHeaderView != null && y > mHeaderView.getTop() && y < mHeaderView.getBottom()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchTarget = getTouchTarget(mHeaderView, x, y);
                mActionDownHappened = true;
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                //TODO
                View target = getTouchTarget(mHeaderView, x, y);
                if (target == mTouchTarget && target.isClickable()) {
                    target.performClick();
                    invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
                } else if (mIsHeaderGroupClickable) {
                    int groupPos = getPackedPositionGroup(getExpandableListPosition(item));
                    if (groupPos != INVALID_POSITION && mActionDownHappened) {
                        if (isGroupExpanded(groupPos))
                            collapseGroup(groupPos);
                        else
                            expandGroup(groupPos);
                    }
                }
                mActionDownHappened = false;
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void refreshHeader() {
        if (mHeaderView == null)
            return;
        int firstVisiblePos = getFirstVisiblePosition();
        int pos = firstVisiblePos + 1;
        int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
        int group = getPackedPositionGroup(getExpandableListPosition(pos));
        if (group == firstVisibleGroupPos + 1) {
            View view = getChildAt(1);
            if (view == null)
                return;
            if (view.getTop() < mHeaderHeight) {
                int delta = mHeaderHeight - view.getTop();
                mHeaderView.layout(0, -delta, mHeaderWidth, mHeaderHeight-delta);
            } else {
                mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
            }
        } else {
            mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
        }

        if (mHeaderUpdateListener != null)
            mHeaderUpdateListener.updatePinnedHeader(mHeaderView, firstVisibleGroupPos);
    }

    private View getTouchTarget(View view, int x, int y) {
        if (!(view instanceof ViewGroup))
            return view;

        View target = null;
        ViewGroup parent = (ViewGroup)view;
        int childCount = parent.getChildCount();
        boolean customOrder = isChildrenDrawingOrderEnabled();
        for (int i = childCount-1; i >= 0; i--) {
            int childIndex = customOrder ? getChildDrawingOrder(childCount, i) : i;
            View child = parent.getChildAt(childIndex);
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }

        return target == null ? parent : target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null || !view.isClickable())
            return false;
        return view.isClickable() && x > view.getLeft() && x < view.getRight() && y > view.getTop() && y < view.getBottom();
    }

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener, boolean isHeaderGroupClickable) {
        super.setOnGroupClickListener(onGroupClickListener);
        mIsHeaderGroupClickable = isHeaderGroupClickable;
    }

    public void setOnHeaderUpdateListener(OnHeaderUpdateListener onHeaderUpdateListener) {
        mHeaderUpdateListener = onHeaderUpdateListener;
        if (onHeaderUpdateListener == null) {
            mHeaderView = null;
            mHeaderHeight = mHeaderWidth = 0;
            return;
        }
        mHeaderView = onHeaderUpdateListener.getPinnedHeader();
        int firstVisiblePos = getFirstVisiblePosition();
        int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
        onHeaderUpdateListener.updatePinnedHeader(mHeaderView, firstVisibleGroupPos);
        requestLayout();
        postInvalidate();
    }

    public interface OnHeaderUpdateListener {
        public View getPinnedHeader();
        public void updatePinnedHeader(View headerView, int firstVisibleGroupPos);
    }
}
