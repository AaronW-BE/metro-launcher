package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dpdns.argv.metrolauncher.model.TileItem;

public class TileView extends FrameLayout {

    protected TextView titleView;
    protected android.widget.ImageView iconView;
    protected android.widget.ImageView unpinButton;
    protected android.widget.ImageView resizeButton;
    protected FrameLayout contentContainer;

    public TileView(Context context) {
        super(context);
        init();
    }

    private android.view.GestureDetector gestureDetector;

    private void init() {
        setClickable(true);
        setFocusable(true);
        
        gestureDetector = new android.view.GestureDetector(getContext(), new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(android.view.MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(android.view.MotionEvent e) {
                // 1. Enter Edit Mode
                MetroHomeView hv = findHomeView();
                if (hv != null) {
                    hv.setEditMode(true);
                }

                // 2. Find ViewHolder and trigger Drag
                android.view.ViewParent p = getParent();
                androidx.recyclerview.widget.RecyclerView rv = null;
                while (p != null) {
                    if (p instanceof androidx.recyclerview.widget.RecyclerView) {
                        rv = (androidx.recyclerview.widget.RecyclerView) p;
                        break;
                    }
                    p = p.getParent();
                }

                if (rv != null) {
                    androidx.recyclerview.widget.RecyclerView.ViewHolder holder = rv.getChildViewHolder(TileView.this);
                    if (holder != null && hv != null) {
                        // Reset global scale/rotation before drag starts
                        animate().scaleX(1f).scaleY(1f).rotationX(0).rotationY(0).setDuration(50).start();
                        hv.startDrag(holder);
                    }
                }
            }

            @Override
            public boolean onSingleTapUp(android.view.MotionEvent e) {
                performClick();
                return true;
            }
        });
        
        int density = (int) getResources().getDisplayMetrics().density;
        int p = 8 * density;

        // Content Container (FrameLayout)
        contentContainer = new FrameLayout(getContext());
        addView(contentContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Icon (Centered, 50% relative to tile)
        iconView = new android.widget.ImageView(getContext());
        iconView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        
        // Initial LayoutParams (Actual size will be set in onMeasure)
        LayoutParams iconLp = new LayoutParams(0, 0); // Placeholder
        iconLp.gravity = Gravity.CENTER;
        contentContainer.addView(iconView, iconLp);

        // Title 
        titleView = new TextView(getContext());
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(12);
        titleView.setGravity(Gravity.BOTTOM | Gravity.START);
        titleView.setPadding(p, 0, p, p);
        
        LayoutParams titleLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleLp.gravity = Gravity.BOTTOM | Gravity.START;
        contentContainer.addView(titleView, titleLp);

        // Edit Mode Buttons
        int btnSize = (int) (24 * density);
        int margin = (int) (4 * density);

        unpinButton = new android.widget.ImageView(getContext());
        unpinButton.setBackgroundColor(0x88000000);
        unpinButton.setPadding(margin, margin, margin, margin);
        unpinButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        LayoutParams unpinLp = new LayoutParams(btnSize, btnSize);
        unpinLp.gravity = Gravity.TOP | Gravity.END;
        unpinLp.topMargin = margin;
        unpinLp.rightMargin = margin;
        addView(unpinButton, unpinLp);

        resizeButton = new android.widget.ImageView(getContext());
        resizeButton.setBackgroundColor(0x88000000);
        resizeButton.setPadding(margin, margin, margin, margin);
        resizeButton.setImageResource(android.R.drawable.ic_menu_crop);
        LayoutParams resizeLp = new LayoutParams(btnSize, btnSize);
        resizeLp.gravity = Gravity.BOTTOM | Gravity.END;
        resizeLp.bottomMargin = margin;
        resizeLp.rightMargin = margin;
        addView(resizeButton, resizeLp);

        unpinButton.setVisibility(GONE);
        resizeButton.setVisibility(GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Enforce icon size to be 50% of the tile's smaller dimension
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        
        if (iconView != null && w > 0 && h > 0) {
            int targetSize = Math.min(w, h) / 2;
            android.view.ViewGroup.LayoutParams lp = iconView.getLayoutParams();
            if (lp != null && (lp.width != targetSize || lp.height != targetSize)) {
                lp.width = targetSize;
                lp.height = targetSize;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private MetroHomeView findHomeView() {
        android.view.ViewParent p = getParent();
        while (p != null && !(p instanceof MetroHomeView)) {
            p = p.getParent();
        }
        return (MetroHomeView) p;
    }

    public void bind(final org.dpdns.argv.metrolauncher.model.TileItem item, boolean isEditMode) {
        setItem(item);
        setBackgroundColor(item.color);
        
        unpinButton.setVisibility(isEditMode ? VISIBLE : GONE);
        resizeButton.setVisibility(isEditMode ? VISIBLE : GONE);

        if (isEditMode) {
            unpinButton.setOnClickListener(v -> {
                MetroHomeView hv = findHomeView();
                if (hv != null) {
                    androidx.recyclerview.widget.RecyclerView rv = null;
                    for (int i = 0; i < hv.getChildCount(); i++) {
                        if (hv.getChildAt(i) instanceof androidx.recyclerview.widget.RecyclerView) {
                            rv = (androidx.recyclerview.widget.RecyclerView) hv.getChildAt(i);
                            break;
                        }
                    }
                    if (rv != null) {
                        MetroTileAdapter adapter = (MetroTileAdapter) rv.getAdapter();
                        // Try both to be safe
                        int pos = rv.getChildLayoutPosition(this);
                        if (pos == androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                            pos = rv.getChildAdapterPosition(this);
                        }
                        
                        Log.d("TileView", "Attempting to remove item at pos: " + pos);
                        if (adapter != null && pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                            adapter.removeItem(pos);
                        }
                    }
                }
            });

            resizeButton.setOnClickListener(v -> {
                // Cycle sizes: 1x1 -> 2x2 -> 4x2 -> 1x1
                if (item.spanX == 1 && item.spanY == 1) {
                    item.spanX = 2; item.spanY = 2;
                } else if (item.spanX == 2 && item.spanY == 2) {
                    item.spanX = 4; item.spanY = 2; // WP standard Large
                } else {
                    item.spanX = 1; item.spanY = 1;
                }
                
                MetroHomeView hv = findHomeView();
                if (hv != null) {
                    for(int i=0; i<hv.getChildCount(); i++) {
                        if(hv.getChildAt(i) instanceof androidx.recyclerview.widget.RecyclerView) {
                            androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) hv.getChildAt(i);
                            MetroTileAdapter adapter = (MetroTileAdapter) rv.getAdapter();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                adapter.saveToStorage();
                            }
                            break;
                        }
                    }
                }
            });
        }
        // Only show label on Large/Medium tiles
        if (titleView != null) {
            titleView.setText(item.title);
            if (item.spanX == 1 && item.spanY == 1) {
                titleView.setVisibility(GONE);
            } else {
                titleView.setVisibility(VISIBLE);
            }
        }
        
        if (iconView != null && item.component != null) {
            try {
                iconView.setImageDrawable(getContext().getPackageManager().getActivityIcon(item.component));
            } catch (Exception e) {
                iconView.setImageDrawable(null);
            }
        }
        
        // Remove default listeners as we handle them in onTouchEvent
        setOnLongClickListener(null);
        setOnClickListener(null);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        // Warning: GestureDetector onDown returns true, which means it consumes DOWN.
        // We must call it first.
        boolean handled = false;
        if (gestureDetector != null) {
            handled = gestureDetector.onTouchEvent(event);
        }
        
        // If detector handled it (e.g. LongPress or Tap), we still want our tilt animation?
        // Actually, if it's a LongPress, we probably want to stop the tilt or keep it?
        // Let's allow the animation to run visually, but we must be careful not to
        // double-consume or break the state machine.
        
        float x = event.getX();
        float y = event.getY();
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                // Calculate tilt
                float dx = x - cx;
                float dy = y - cy;
                
                // Max rotation degrees
                float maxTilt = 10f;
                
                // Improve feel: scale down slightly
                animate().scaleX(0.96f).scaleY(0.96f)
                        .rotationY((dx / cx) * maxTilt)
                        .rotationX(-(dy / cy) * maxTilt)
                        .setDuration(150)
                        .start();
                // Return true to ensure we receive subsequent events
                return true; 

            case android.view.MotionEvent.ACTION_UP:
                animate().scaleX(1f).scaleY(1f)
                        .rotationX(0).rotationY(0)
                        .setDuration(150)
                        .start();
                break;

            case android.view.MotionEvent.ACTION_CANCEL:
                animate().scaleX(1f).scaleY(1f)
                        .rotationX(0).rotationY(0)
                        .setDuration(150)
                        .start();
                break;
        }

        return handled || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        
        MetroHomeView hv = findHomeView();
        if (hv != null && hv.isEditMode()) {
            hv.setEditMode(false);
            return true;
        }

        if (this.mItem != null) {
             android.view.ViewParent parent = getParent();
             while (parent != null && !(parent instanceof MetroHomeView)) {
                 parent = parent.getParent();
             }

             if (parent instanceof MetroHomeView && this.mItem.component != null) {
                final String pkgName = this.mItem.component.getPackageName();
                ((MetroHomeView) parent).performTurnExit(() -> {
                    android.content.Intent intent = getContext().getPackageManager()
                            .getLaunchIntentForPackage(pkgName);
                    if (intent != null) {
                        getContext().startActivity(intent);
                    }
                });
            } else if (this.mItem.component != null) {
                // Fallback
                android.content.Intent intent = getContext().getPackageManager()
                        .getLaunchIntentForPackage(this.mItem.component.getPackageName());
                if (intent != null) getContext().startActivity(intent);
            }
        }
        return true;
    }

    private org.dpdns.argv.metrolauncher.model.TileItem mItem;

    private void setItem(org.dpdns.argv.metrolauncher.model.TileItem item) {
        this.mItem = item;
    }
}
