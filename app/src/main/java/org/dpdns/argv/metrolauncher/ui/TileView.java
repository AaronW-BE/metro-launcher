package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dpdns.argv.metrolauncher.model.TileItem;

public class TileView extends FrameLayout {

    private TextView titleView;
    private android.widget.ImageView iconView;

    public TileView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        
        int density = (int) getResources().getDisplayMetrics().density;
        int p = 8 * density;

        // Icon (Centered, Unified Size)
        iconView = new android.widget.ImageView(getContext());
        iconView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        
        int iconSize = 40 * density; // Unified Icon Size
        LayoutParams iconLp = new LayoutParams(iconSize, iconSize);
        iconLp.gravity = Gravity.CENTER;
        addView(iconView, iconLp);

        // Title (Bottom Left, Small)
        titleView = new TextView(getContext());
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(12); // Metro standard
        titleView.setGravity(Gravity.BOTTOM | Gravity.START);
        titleView.setPadding(p, 0, p, (int)(p * 0.5));
        
        LayoutParams titleLp = new LayoutParams(
            LayoutParams.WRAP_CONTENT, 
            LayoutParams.WRAP_CONTENT
        );
        titleLp.gravity = Gravity.BOTTOM | Gravity.START;
        addView(titleView, titleLp);
    }

    public void bind(final org.dpdns.argv.metrolauncher.model.TileItem item) {
        setBackgroundColor(item.color);
        
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

        setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle(item.title)
                    .setItems(new String[]{"从开始屏幕取消固定"}, (d, i) -> {
                        if (i == 0) {
                            // Find adapter and call removeItem
                            android.view.ViewParent p = getParent();
                            while (p != null && !(p instanceof androidx.recyclerview.widget.RecyclerView)) {
                                p = p.getParent();
                            }
                            if (p instanceof androidx.recyclerview.widget.RecyclerView) {
                                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) p;
                                MetroTileAdapter adapter = (MetroTileAdapter) rv.getAdapter();
                                if (adapter != null) {
                                    int pos = rv.getChildAdapterPosition(v);
                                    if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                        adapter.removeItem(pos);
                                    }
                                }
                            }
                        }
                    })
                    .show();
            return true;
        });

        setOnClickListener(v -> {
            // 1. Visual feedback on the tile itself
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                
                // 2. Find MetroHomeView to perform the "Turn" exit animation
                android.view.ViewParent parent = getParent();
                while (parent != null && !(parent instanceof MetroHomeView)) {
                    parent = parent.getParent();
                }
                
                if (parent instanceof MetroHomeView && item.component != null) {
                    final String pkgName = item.component.getPackageName();
                    ((MetroHomeView) parent).performTurnExit(() -> {
                        android.content.Intent intent = getContext().getPackageManager()
                                .getLaunchIntentForPackage(pkgName);
                        if (intent != null) {
                            getContext().startActivity(intent);
                        }
                    });
                } else if (item.component != null) {
                    // Fallback if view hierarchy is different
                    android.content.Intent intent = getContext().getPackageManager()
                            .getLaunchIntentForPackage(item.component.getPackageName());
                    if (intent != null) getContext().startActivity(intent);
                }
            }).start();
        });
    }
}
