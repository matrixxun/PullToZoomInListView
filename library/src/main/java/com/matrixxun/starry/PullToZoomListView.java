package com.matrixxun.starry;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

public class PullToZoomListView extends ListView implements OnScrollListener {
    private static final int INVALID_VALUE = -1;
    private static float DEFAULT_MIN_SCALE = 1.0f;
    private static final String TAG = "PullToZoomListView";
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= DEFAULT_MIN_SCALE;
            return ((((t * t) * t) * t) * t) + DEFAULT_MIN_SCALE;
        }
    };
    int mActivePointerId = INVALID_VALUE;
    private FrameLayout mHeaderContainer;
    private int mHeaderHeight;
    private ImageView mHeaderImage;
    float mLastMotionY = INVALID_VALUE;
    float mLastScale = INVALID_VALUE;
    float mMaxScale = INVALID_VALUE;
    private OnScrollListener mOnScrollListener;
    private ScalingRunnable mScalingRunnable;
    private int mScreenHeight;
    private boolean mScrollable = true;
    private ImageView mShadow;
    private boolean mShowHeaderImage = true;
    private boolean mZoomable = true;

    class ScalingRunnable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        public boolean isFinished() {
            return mIsFinished;
        }

        public void abortAnimation() {
            mIsFinished = true;
        }

        public void startAnimation(long duration) {
            mStartTime = SystemClock.currentThreadTimeMillis();
            mDuration = duration;
            mScale = ((float) mHeaderContainer.getBottom()) / ((float) mHeaderHeight);
            mIsFinished = false;
            post(this);
        }

        public void run() {
            if (!mIsFinished && ((double) mScale) > 1.0d) {
                float scale = mScale - ((mScale - DEFAULT_MIN_SCALE) * sInterpolator.getInterpolation((((float) SystemClock.currentThreadTimeMillis()) - ((float) mStartTime)) / ((float) mDuration)));
                ViewGroup.LayoutParams params = mHeaderContainer.getLayoutParams();
                if (scale <= DEFAULT_MIN_SCALE) {
                    mIsFinished = true;
                    params.height = mHeaderHeight;
                } else {
                    params.height = (int) (((float) mHeaderHeight) * scale);
                }
                mHeaderContainer.setLayoutParams(params);
                post(this);
            }
        }
    }

    public PullToZoomListView(Context context) {
        super(context);
        init(context);
    }

    public PullToZoomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToZoomListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        mHeaderContainer = new FrameLayout(context);
        mHeaderImage = new ImageView(context);
        int width = metrics.widthPixels;
        setHeaderViewSize(width, (int) ((((float) width) / 16.0f) * 9.0f));
        mShadow = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2);
        params.gravity = 80;
        mShadow.setLayoutParams(params);
        mHeaderContainer.addView(mHeaderImage);
        mHeaderContainer.addView(mShadow);
        addHeaderView(mHeaderContainer);
        mScalingRunnable = new ScalingRunnable();
        super.setOnScrollListener(this);
    }

    public ImageView getHeaderView() {
        return mHeaderImage;
    }

    public void setShadow(int resId) {
        if (mShowHeaderImage) {
            mShadow.setBackgroundResource(resId);
        }
    }

    public void setHeaderViewSize(int width, int height) {
        if (mShowHeaderImage) {
            ViewGroup.LayoutParams params = mHeaderContainer.getLayoutParams();
            if (params == null) {
                params = new AbsListView.LayoutParams(width, height);
            }
            params.width = width;
            params.height = height;
            mHeaderContainer.setLayoutParams(params);
            mHeaderHeight = height;
        }
    }

    public void setZoomable(boolean zoomable) {
        if (mShowHeaderImage) {
            mZoomable = zoomable;
        }
    }

    public boolean isZoomable() {
        return mZoomable;
    }

    public void setScrollable(boolean scrollable) {
        if (mShowHeaderImage) {
            mScrollable = scrollable;
        }
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public void hideHeaderImage() {
        mShowHeaderImage = false;
        mZoomable = false;
        mScrollable = false;
        removeHeaderView(mHeaderContainer);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeaderHeight == 0) {
            mHeaderHeight = mHeaderContainer.getHeight();
        }
    }

    private void reset() {
        mActivePointerId = INVALID_VALUE;
        mLastMotionY = INVALID_VALUE;
        mMaxScale = INVALID_VALUE;
        mLastScale = INVALID_VALUE;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mZoomable) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mMaxScale = ((float) mScreenHeight) / ((float) mHeaderHeight);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mActivePointerId = ev.getPointerId(ev.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mZoomable) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScalingRunnable.mIsFinished) {
                    mScalingRunnable.abortAnimation();
                }
                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mMaxScale = ((float) mScreenHeight) / ((float) mHeaderHeight);
                mLastScale = ((float) mHeaderContainer.getBottom()) / ((float) mHeaderHeight);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                endScaling();
                break;
            case MotionEvent.ACTION_MOVE:
                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex != INVALID_VALUE) {
                    if (mLastMotionY == INVALID_VALUE) {
                        mLastMotionY = ev.getY(activePointerIndex);
                    }
                    if (mHeaderContainer.getBottom() < mHeaderHeight) {
                        mLastMotionY = ev.getY(activePointerIndex);
                        break;
                    }
                    ViewGroup.LayoutParams params = mHeaderContainer.getLayoutParams();
                    float scale = ((((((float) mHeaderContainer.getBottom()) + (ev.getY(activePointerIndex) - mLastMotionY)) / ((float) mHeaderHeight)) - mLastScale) / 2.0f) + mLastScale;
                    if (((double) mLastScale) > 1.0d || scale >= mLastScale) {
                        mLastScale = Math.min(Math.max(scale, DEFAULT_MIN_SCALE), mMaxScale);
                        params.height = (int) (((float) mHeaderHeight) * mLastScale);
                        if (params.height < mScreenHeight) {
                            mHeaderContainer.setLayoutParams(params);
                        }
                        mLastMotionY = ev.getY(activePointerIndex);
                        return true;
                    }
                    params.height = mHeaderHeight;
                    mHeaderContainer.setLayoutParams(params);
                    return super.onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                int index = ev.getActionIndex();
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
        if (ev.getPointerId(pointerIndex) == mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void endScaling() {
        if (mHeaderContainer.getBottom() >= mHeaderHeight) {
            mScalingRunnable.startAnimation(200);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mScrollable) {
            float scrollY = (float) (mHeaderHeight - mHeaderContainer.getBottom());
            if (scrollY > 0.0f && scrollY < ((float) mHeaderHeight)) {
                mHeaderImage.scrollTo(0, -((int) (((double) scrollY) * 0.65d)));
            } else if (mHeaderImage.getScrollY() != 0) {
                mHeaderImage.scrollTo(0, 0);
            }
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }
}