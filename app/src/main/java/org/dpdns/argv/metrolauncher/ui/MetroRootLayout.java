package org.dpdns.argv.metrolauncher.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MetroRootLayout extends ViewGroup {

    private static final int STATE_HOME = 0;
    private static final int STATE_APP_LIST = 1;

    private int currentState = STATE_HOME;

    private float downX;
    private float downY;
    private float lastX;

    private float offsetX; // 关键：当前整体偏移量（[-width, 0]）

    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int minFlingVelocity;

    private ValueAnimator settleAnimator;
    private OnScrollListener scrollListener;

    public interface OnScrollListener {
        void onScroll(float offset, float maxOffset);
        void onStateChanged(int newState);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.scrollListener = listener;
    }

    public MetroRootLayout(@NonNull Context context) {
        this(context, null);
    }

    public MetroRootLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MetroRootLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();

        View home = getChildAt(0);
        View appList = getChildAt(1);

        int homeLeft = (int) offsetX;
        home.layout(homeLeft, 0, homeLeft + width, height);

        int appLeft = homeLeft + width;
        appList.layout(appLeft, 0, appLeft + width, height);
    }


    // ------------------------------------------------
    // Touch
    // ------------------------------------------------
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                lastX = downX;
                obtainVelocityTracker();
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - downX);
                float dy = Math.abs(ev.getY() - downY);
                if (dx > touchSlop && dx > dy) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                releaseVelocityTracker();
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker();
        velocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float dx = x - lastX;
                lastX = x;

                offsetX += dx;

                // 阻尼（非常关键，WP 手感）
                if (offsetX > 0) {
                    offsetX *= 0.35f;
                }
                if (offsetX < -getWidth()) {
                    offsetX = -getWidth() + (offsetX + getWidth()) * 0.35f;
                }

                if (scrollListener != null) {
                    scrollListener.onScroll(offsetX, -getWidth());
                }

                requestLayout();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.computeCurrentVelocity(1000);
                float vx = velocityTracker.getXVelocity();
                releaseVelocityTracker();
                finishScroll(vx);
                break;
        }
        return true;
    }

    // ------------------------------------------------
    // Scroll Logic
    // ------------------------------------------------

    private void finishScroll(float velocityX) {
        int width = getWidth();
        float target;

        if (Math.abs(velocityX) > minFlingVelocity) {
            if (velocityX < 0) {
                target = -width;
                currentState = STATE_APP_LIST;
            } else {
                target = 0;
                currentState = STATE_HOME;
            }
        } else {
            if (offsetX < -width / 2f) {
                target = -width;
                currentState = STATE_APP_LIST;
            } else {
                target = 0;
                currentState = STATE_HOME;
            }
        }

        if (scrollListener != null) {
            scrollListener.onStateChanged(currentState);
        }

        animateTo(target);
    }

    private void animateTo(float target) {
        if (settleAnimator != null && settleAnimator.isRunning()) {
            settleAnimator.cancel();
        }

        settleAnimator = ValueAnimator.ofFloat(offsetX, target);
        settleAnimator.setDuration(280);
        settleAnimator.setInterpolator(new DecelerateInterpolator());
        settleAnimator.addUpdateListener(animation -> {
            offsetX = (float) animation.getAnimatedValue();
            if (scrollListener != null) {
                scrollListener.onScroll(offsetX, -getWidth());
            }
            requestLayout();
        });
        settleAnimator.start();
    }

    // ------------------------------------------------
    // Public API
    // ------------------------------------------------

    public void showHome() {
        currentState = STATE_HOME;
        animateTo(0f);
    }

    public void showAppList() {
        currentState = STATE_APP_LIST;
        animateTo(-getWidth());
    }

    public boolean isShowingAppList() {
        return currentState == STATE_APP_LIST;
    }

    // ------------------------------------------------
    // VelocityTracker
    // ------------------------------------------------

    private void obtainVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

}
