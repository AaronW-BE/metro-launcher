package org.dpdns.argv.metrolauncher.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import org.dpdns.argv.metrolauncher.model.TileItem;

/**
 * Base class for tiles that have animated content (Live Tiles).
 * Supports a "Flip" animation between a Front and Back view.
 */
public abstract class LiveTileView extends TileView {

    protected View frontView;
    protected View backView;
    
    private boolean isShowingBack = false;
    private boolean isAnimating = false;
    
    private final Runnable flipRunnable = this::flip;
    private boolean isRunning = false;

    public LiveTileView(Context context) {
        super(context);
    }

    protected abstract View createFrontView();
    protected abstract View createBackView();
    
    protected void onFlipStart(View nextView) {}

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initViews();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }
    
    // Call this inside bind()
    protected void initViews() {
        if (frontView == null) {
            // NOTE: We no longer removeAllViews here because it strips the default icon/title
            // that subclasses might want to keep as fallback until "Live" data is ready.
            
            frontView = createFrontView();
            backView = createBackView();

            if (frontView != null) {
                if (frontView.getLayoutParams() == null) {
                    frontView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
                frontView.setVisibility(GONE); // VITAL: Don't obscure fallback icon until ready
                if (contentContainer != null) contentContainer.addView(frontView);
                else addView(frontView);
            }
            if (backView != null) {
                if (backView.getLayoutParams() == null) {
                    backView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
                if (contentContainer != null) contentContainer.addView(backView);
                else addView(backView);
                
                backView.setAlpha(0f);
                backView.setVisibility(GONE);
                backView.setRotationX(-90f);
            }
        }
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        // Random start delay to avoid all tiles flipping at once
        long delay = 3000 + (long)(Math.random() * 5000);
        postDelayed(flipRunnable, delay);
    }

    public void stop() {
        isRunning = false;
        removeCallbacks(flipRunnable);
    }

    private void flip() {
        if (!isRunning || isAnimating || backView == null) return;
        
        isAnimating = true;

        final View visibleView = isShowingBack ? backView : frontView;
        final View hiddenView = isShowingBack ? frontView : backView;
        
        if (visibleView == null || hiddenView == null) return;

        onFlipStart(hiddenView);

        hiddenView.setVisibility(VISIBLE);
        
        // 1. Rotate visible out
        ObjectAnimator outAnim = ObjectAnimator.ofFloat(visibleView, "rotationX", 0f, 90f);
        outAnim.setDuration(400);
        outAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        outAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                visibleView.setVisibility(GONE);
                visibleView.setRotationX(0f); // Reset
                
                // 2. Rotate hidden in
                hiddenView.setRotationX(-90f);
                hiddenView.setAlpha(1f);
                ObjectAnimator inAnim = ObjectAnimator.ofFloat(hiddenView, "rotationX", -90f, 0f);
                inAnim.setDuration(400);
                inAnim.setInterpolator(new DecelerateInterpolator());
                inAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimating = false;
                        isShowingBack = !isShowingBack;
                        
                        // Schedule next flip
                        if (isRunning) {
                             long delay = 5000 + (long)(Math.random() * 8000);
                             postDelayed(flipRunnable, delay);
                        }
                    }
                });
                inAnim.start();
            }
        });
        outAnim.start();
    }
}
