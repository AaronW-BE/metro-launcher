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
                new MetroTileLayoutManager(context, 4); // 4 列（WP 标准）

        recyclerView.setLayoutManager(layoutManager);

        adapter = new MetroTileAdapter(context);
        recyclerView.setAdapter(adapter);

        // 关键：纵向滚动时，禁止父容器抢事件
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });

        addView(recyclerView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

//        List<PinnedTile> tiles = TileStorage.load(context);
//        adapter.setTiles(tiles);

        TileChangeBus.register(this);
    }

    public void setTiles(java.util.List<TileItem> tiles) {
        adapter.setTiles(tiles);
    }

    @Override
    public void onTilesChanged() {

    }
}
