package com.matrixxun.starry;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created by matrixxun on 2017/5/10.
 */
public class PullToZoomListView extends ListView implements
        AbsListView.OnScrollListener {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "PullToZoomListView";
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0f;
            return 1.0f + f * (f * (f * (f * f)));
        }
    };
    private FrameLayout mHeaderContainer;
    private ImageView mHeaderImage;
    private ImageView mShadow;
    private AbsListView.OnScrollListener mOnScrollListener;
    private ScalingRunnable mScalingRunnable;

    private int mHeaderHeight;
    private int mScreenHeight;
    private int mActivePointerId = -1;
    private float mLastMotionY = -1.0f;
    private float mLastScale = -1.0f;
    private float mMaxScale = -1.0f;


    public PullToZoomListView(Context paramContext) {
        super(paramContext);
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public PullToZoomListView(Context paramContext,
                              AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(isInEditMode()){
            return;
        }

        init(getContext());
    }

    private void endScaling() {
        if (mHeaderContainer.getBottom() >= mHeaderHeight) {
            mScalingRunnable.startAnimation(100L);
        }
    }

    private void init(Context paramContext) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        mScreenHeight = localDisplayMetrics.heightPixels;
        mHeaderContainer = new FrameLayout(paramContext);
        mHeaderImage = new ImageView(paramContext);
        int i = localDisplayMetrics.widthPixels;
        setHeaderViewSize(i, (int) (9.0f * (i / 16.0f)));
        mShadow = new ImageView(paramContext);
        FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -2);
        localLayoutParams.gravity = 80;
        mShadow.setLayoutParams(localLayoutParams);
        mHeaderContainer.addView(mHeaderImage);
        mHeaderContainer.addView(mShadow);
        addHeaderView(mHeaderContainer);
        mScalingRunnable = new ScalingRunnable();
        super.setOnScrollListener(this);
    }

    private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
        int i = (paramMotionEvent.getAction()) >> 8;
        if (paramMotionEvent.getPointerId(i) == mActivePointerId){
            if (i != 0) {
                mLastMotionY = paramMotionEvent.getY(0);
                mActivePointerId = paramMotionEvent.getPointerId(0);
                return;
            }
        }
    }

    private void reset() {
        mActivePointerId = -1;
        mLastMotionY = -1.0f;
        mMaxScale = -1.0f;
        mLastScale = -1.0f;
    }

    public ImageView getHeaderView() {
        return mHeaderImage;
    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        if (mHeaderHeight == 0){
            mHeaderHeight = mHeaderContainer.getHeight();
        }
    }

    @Override
    public void onScroll(AbsListView paramAbsListView, int paramInt1,int paramInt2, int paramInt3) {
        float f = mHeaderHeight -  mHeaderContainer.getBottom();
        if ((f > 0.0f) && (f < mHeaderHeight)) {
            int i = (int) (0.65d * f);
            mHeaderImage.scrollTo(0, -i);
        } else if (mHeaderImage.getScrollY() != 0) {
            mHeaderImage.scrollTo(0, 0);
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(paramAbsListView, paramInt1,paramInt2, paramInt3);
        }
    }

    public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(paramAbsListView, paramInt);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_DOWN:
                if (!mScalingRunnable.mIsFinished) {
                    mScalingRunnable.abortAnimation();
                }
                 mLastMotionY = motionEvent.getY();
                 mActivePointerId = motionEvent.getPointerId(0);
                 mMaxScale = ( mScreenHeight / mHeaderHeight);
                 mLastScale = ( mHeaderContainer.getBottom() /  mHeaderHeight);
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = motionEvent.findPointerIndex( mActivePointerId);
                if (pointerIndex == -1) {
                } else {
                    if ( mLastMotionY == -1.0f)
                         mLastMotionY = motionEvent.getY(pointerIndex);
                    if ( mHeaderContainer.getBottom() >=  mHeaderHeight) {
                        ViewGroup.LayoutParams localLayoutParams =  mHeaderContainer.getLayoutParams();
                        float f = ((motionEvent.getY(pointerIndex) -  mLastMotionY +  mHeaderContainer.getBottom()) /  mHeaderHeight -  mLastScale) / 2.0f +  mLastScale;
                        if (( mLastScale <= 1.0d) && (f <  mLastScale)) {
                            localLayoutParams.height =  mHeaderHeight;
                             mHeaderContainer.setLayoutParams(localLayoutParams);
                            return super.onTouchEvent(motionEvent);
                        }
                         mLastScale = Math.min(Math.max(f, 1.0f), mMaxScale);
                        localLayoutParams.height = ((int) ( mHeaderHeight *  mLastScale));
                        if (localLayoutParams.height <  mScreenHeight)
                            mHeaderContainer.setLayoutParams(localLayoutParams);
                        mLastMotionY = motionEvent.getY(pointerIndex);
                        return true;
                    }
                     mLastMotionY = motionEvent.getY(pointerIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                endScaling();
                break;
            case 3:
                int i = motionEvent.getActionIndex();
                 mLastMotionY = motionEvent.getY(i);
                 mActivePointerId = motionEvent.getPointerId(i);
                break;
            case 5:
                onSecondaryPointerUp(motionEvent);
                 mLastMotionY = motionEvent.getY(motionEvent
                        .findPointerIndex( mActivePointerId));
                break;
            case 6:
        }
        return super.onTouchEvent(motionEvent);
    }

    public void setHeaderViewSize(int paramInt1, int paramInt2) {
        Object localObject = mHeaderContainer.getLayoutParams();
        if (localObject == null){
            localObject = new AbsListView.LayoutParams(paramInt1, paramInt2);
        }
        ((ViewGroup.LayoutParams) localObject).width = paramInt1;
        ((ViewGroup.LayoutParams) localObject).height = paramInt2;
        mHeaderContainer.setLayoutParams((ViewGroup.LayoutParams) localObject);
        mHeaderHeight = paramInt2;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener paramOnScrollListener) {
        mOnScrollListener = paramOnScrollListener;
    }

    public void setShadow(int paramInt) {
        mShadow.setBackgroundResource(paramInt);
    }

    class ScalingRunnable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        public void abortAnimation() {
            mIsFinished = true;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public void run() {
            float f2;
            ViewGroup.LayoutParams localLayoutParams;
            if ((!mIsFinished) && (mScale > 1.0d)) {
                float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                f2 = mScale - (mScale - 1.0f) * sInterpolator.getInterpolation(f1);
                localLayoutParams = mHeaderContainer.getLayoutParams();
                if (f2 > 1.0f) {
                    localLayoutParams.height = mHeaderHeight;
                    localLayoutParams.height = ((int) (f2 * mHeaderHeight));
                    mHeaderContainer.setLayoutParams(localLayoutParams);
                    post(this);
                    return;
                }
                mIsFinished = true;
            }
        }

        public void startAnimation(long paramLong) {
            mStartTime = SystemClock.currentThreadTimeMillis();
            mDuration = paramLong;
            mScale = ((float) (mHeaderContainer.getBottom()) / mHeaderHeight);
            mIsFinished = false;
            post(this);
        }
    }
}
