package org.dpdns.argv.metrolauncher.ui;


import android.content.Context;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.dpdns.argv.metrolauncher.model.TileItem;

import java.util.ArrayList;
import java.util.List;

public class MetroTileLayoutManager extends RecyclerView.LayoutManager {

    private final int columnCount;
    private int cellSize;

    private int verticalScrollOffset = 0;
    private int totalHeight = 0;

    private final SparseArray<Rect> itemRects = new SparseArray<>();

    public MetroTileLayoutManager(Context context, int columnCount) {
        this.columnCount = columnCount;
    }

    // ------------------------------------------------
    // Required Overrides
    // ------------------------------------------------


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new MetroTileLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    // ------------------------------------------------
    // Layout
    // ------------------------------------------------

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0 || state.isPreLayout()) {
            detachAndScrapAttachedViews(recycler);
            return;
        }

        detachAndScrapAttachedViews(recycler);
        itemRects.clear();
        verticalScrollOffset = 0;

        cellSize = getWidth() / columnCount;

        // 动态占位表（row 可扩展）
        List<boolean[]> occupancy = new ArrayList<>();

        int maxRowUsed = 0;

        for (int i = 0; i < getItemCount(); i++) {

            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            MetroTileLayoutParams lp =
                    (MetroTileLayoutParams) view.getLayoutParams();

            int spanX = lp.spanX;
            int spanY = lp.spanY;

            int[] pos = findPosition(occupancy, spanX, spanY);
            int col = pos[0];
            int row = pos[1];

            markOccupied(occupancy, col, row, spanX, spanY);

            int left = col * cellSize;
            int top = row * cellSize;
            int right = left + spanX * cellSize;
            int bottom = top + spanY * cellSize;

            Rect rect = new Rect(left, top, right, bottom);
            itemRects.put(i, rect);

            detachAndScrapView(view, recycler);
        }

        totalHeight = maxRowUsed * cellSize;

        fill(recycler, state);
    }

    // ------------------------------------------------
    // Scrolling
    // ------------------------------------------------

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int travel = dy;

        if (verticalScrollOffset + dy < 0) {
            travel = -verticalScrollOffset;
        } else if (verticalScrollOffset + dy > totalHeight - getHeight()) {
            travel = Math.max(0, totalHeight - getHeight() - verticalScrollOffset);
        }

        verticalScrollOffset += travel;
        offsetChildrenVertical(-travel);

        fill(recycler, state);

        return travel;
    }

    // ------------------------------------------------
    // Fill / Recycle
    // ------------------------------------------------

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) return;

        Rect displayRect = new Rect(
                0,
                verticalScrollOffset,
                getWidth(),
                verticalScrollOffset + getHeight()
        );

        // 回收
        for (int i = 0; i < getChildCount(); ) {
            View child = getChildAt(i);
            int pos = getPosition(child);
            Rect rect = itemRects.get(pos);

            if (!Rect.intersects(displayRect, rect)) {
                removeAndRecycleView(child, recycler);
            } else {
                i++;
            }
        }

        // 填充
        for (int i = 0; i < getItemCount(); i++) {
            Rect rect = itemRects.get(i);
            if (Rect.intersects(displayRect, rect)) {
                View view = findViewByPosition(i);
                if (view == null) {
                    view = recycler.getViewForPosition(i);
                    addView(view);

                    measureChildWithMargins(view, 0, 0);
                    layoutDecorated(
                            view,
                            rect.left,
                            rect.top - verticalScrollOffset,
                            rect.right,
                            rect.bottom - verticalScrollOffset
                    );
                }
            }
        }
    }

    // ------------------------------------------------
    // Occupancy Algorithm
    // ------------------------------------------------

    private int[] findPosition(List<boolean[]> occupancy, int spanX, int spanY) {
        int row = 0;

        while (true) {
            ensureRows(occupancy, row + spanY);

            for (int col = 0; col <= columnCount - spanX; col++) {
                if (canPlace(occupancy, col, row, spanX, spanY)) {
                    return new int[]{col, row};
                }
            }
            row++;
        }
    }

    private boolean canPlace(List<boolean[]> occupancy, int col, int row, int spanX, int spanY) {
        for (int y = row; y < row + spanY; y++) {
            boolean[] rowOcc = occupancy.get(y);
            for (int x = col; x < col + spanX; x++) {
                if (rowOcc[x]) return false;
            }
        }
        return true;
    }

    private void markOccupied(List<boolean[]> occupancy, int col, int row, int spanX, int spanY) {
        for (int y = row; y < row + spanY; y++) {
            boolean[] rowOcc = occupancy.get(y);
            for (int x = col; x < col + spanX; x++) {
                rowOcc[x] = true;
            }
        }
    }

    private void ensureRows(List<boolean[]> occupancy, int rows) {
        while (occupancy.size() < rows) {
            occupancy.add(new boolean[columnCount]);
        }
    }
}
