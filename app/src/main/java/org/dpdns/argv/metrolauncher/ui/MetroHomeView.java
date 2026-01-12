package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.dpdns.argv.metrolauncher.TileChangeBus;
import org.dpdns.argv.metrolauncher.model.OnTilesChangedListener;
import org.dpdns.argv.metrolauncher.model.PinnedTile;
import org.dpdns.argv.metrolauncher.model.TileItem;
import org.dpdns.argv.metrolauncher.model.TileStorage;

import java.util.List;

public class MetroHomeView extends FrameLayout implements OnTilesChangedListener {

    private RecyclerView recyclerView;
    private MetroTileAdapter adapter;
    private androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper;
    private boolean isEditMode = false;

    public MetroHomeView(@NonNull Context context) {
        this(context, null);
    }

    public MetroHomeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MetroHomeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        recyclerView = new RecyclerView(context);
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);

        MetroTileLayoutManager layoutManager =
                new MetroTileLayoutManager(context, 6); // 6 列（WP8.1/10 高密度布局）

        recyclerView.setLayoutManager(layoutManager);

        adapter = new MetroTileAdapter(context);
        recyclerView.setAdapter(adapter);

        // 关键：纵向滚动时，禁止父容器抢事件
        // Fix: Removed requestDisallowInterceptTouchEvent to allow horizontal swipe on blank areas
        // recyclerView.setOnTouchListener((v, event) -> {
        //     if (event.getAction() == MotionEvent.ACTION_MOVE) {
        //         v.getParent().requestDisallowInterceptTouchEvent(true);
        //     }
        //     return false;
        // });

        addView(recyclerView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        // Drag and Drop (ItemTouchHelper)
        androidx.recyclerview.widget.ItemTouchHelper.Callback callback = 
            new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                androidx.recyclerview.widget.ItemTouchHelper.UP | 
                androidx.recyclerview.widget.ItemTouchHelper.DOWN | 
                androidx.recyclerview.widget.ItemTouchHelper.LEFT | 
                androidx.recyclerview.widget.ItemTouchHelper.RIGHT, 0) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (viewHolder != null) {
                        viewHolder.itemView.setAlpha(0.8f);
                        viewHolder.itemView.setScaleX(1.1f);
                        viewHolder.itemView.setScaleY(1.1f);
                    }
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // We trigger it manually from TileView
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1.0f);
                viewHolder.itemView.setScaleX(1.0f);
                viewHolder.itemView.setScaleY(1.0f);
            }
        };

        itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        List<PinnedTile> saved = TileStorage.load(context);
        List<TileItem> items = new java.util.ArrayList<>();
        long idCounter = 0;
        for (PinnedTile t : saved) {
            items.add(TileItem.fromPinned(context, t, idCounter++));
        }
        adapter.setTiles(items);

        // Initialize Gesture Detector for blank area clicks
        android.view.GestureDetector gestureDetector = new android.view.GestureDetector(context, new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                // Only handle if in Edit Mode
                if (!isEditMode) return false;

                // Check if we hit a child
                android.view.View child = rv.findChildViewUnder(e.getX(), e.getY());
                
                // If we didn't hit a child, and it's a confirmed tap
                if (child == null && gestureDetector.onTouchEvent(e)) {
                    setEditMode(false);
                    return true; // Consume the event
                }
                return false;
            }
        });

        TileChangeBus.register(this);
    }

    public void setEditMode(boolean editMode) {
        if (this.isEditMode == editMode) return;
        this.isEditMode = editMode;
        if (adapter != null) {
            adapter.setEditMode(editMode);
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void startDrag(RecyclerView.ViewHolder holder) {
        if (itemTouchHelper != null) {
            itemTouchHelper.startDrag(holder);
        }
    }

    public void setTiles(java.util.List<TileItem> tiles) {
        adapter.setTiles(tiles);
    }

    public void performTurnExit(final Runnable onComplete) {
        int childCount = recyclerView.getChildCount();
        if (childCount == 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Find max coordinates to calculate delays (bottom-right is 0, top-left is max)
        int maxTop = 0;
        int maxLeft = 0;
        for (int i = 0; i < childCount; i++) {
            android.view.View child = recyclerView.getChildAt(i);
            if (child.getTop() > maxTop) maxTop = child.getTop();
            if (child.getLeft() > maxLeft) maxLeft = child.getLeft();
        }

        long lastAnimationStartTime = 0;
        for (int i = 0; i < childCount; i++) {
            final android.view.View child = recyclerView.getChildAt(i);
            
            // Calculate delay: further from bottom-right = more delay
            // Using a factor for better "sweep" speed - reduced for faster launch
            long delay = (long) ((maxTop - child.getTop() + maxLeft - child.getLeft()) * 0.08f);
            if (delay > lastAnimationStartTime) lastAnimationStartTime = delay;

            child.setPivotX(child.getWidth());
            child.setPivotY(child.getHeight() / 2f);
            child.setCameraDistance(5000 * getResources().getDisplayMetrics().density);

            child.animate()
                .rotationY(-90f)
                .scaleX(0.7f)
                .scaleY(0.7f)
                .alpha(0f)
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .start();
        }

        // Call onComplete after a safe duration (max delay + animation duration)
        // Reduced waiting buffer
        postDelayed(() -> {
            if (onComplete != null) onComplete.run();
            
            // Reset state for when we return
            postDelayed(() -> {
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    android.view.View child = recyclerView.getChildAt(i);
                    child.setRotationY(0f);
                    child.setScaleX(1.0f);
                    child.setScaleY(1.0f);
                    child.setAlpha(1.0f);
                }
            }, 300);
        }, lastAnimationStartTime + 50); // 50ms overlap for snappier launch
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        org.dpdns.argv.metrolauncher.TileChangeBus.unregister(this);
    }

    @Override
    public void onTilesChanged() {
        android.view.ContextThemeWrapper context = (android.view.ContextThemeWrapper) getContext();
        List<PinnedTile> saved = TileStorage.load(context);
        List<TileItem> items = new java.util.ArrayList<>();
        long idCounter = 0;
        for (PinnedTile t : saved) {
            items.add(TileItem.fromPinned(context, t, idCounter++));
        }
        setTiles(items);
    }
}
