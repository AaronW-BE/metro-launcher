package org.dpdns.argv.metrolauncher.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dpdns.argv.metrolauncher.AppLoader;
import org.dpdns.argv.metrolauncher.TileChangeBus;
import org.dpdns.argv.metrolauncher.model.AppInfo;
import org.dpdns.argv.metrolauncher.model.AppListItem;
import org.dpdns.argv.metrolauncher.model.PinnedTile;
import org.dpdns.argv.metrolauncher.model.TileStorage;

import java.util.List;

public class AppListView extends FrameLayout {

    private RecyclerView recyclerView;
    private LetterIndexView letterIndexView;

    private AppListAdapter adapter;

    public AppListView(Context context) {
        super(context);
        init();
    }

    public AppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);

        adapter = new AppListAdapter();
        adapter.setOnHeaderClickListener(() -> {
            letterIndexView.setAlpha(0f);
            letterIndexView.setVisibility(VISIBLE);
            letterIndexView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start();
        });
        adapter.setOnPinListener(app -> {
            pinToStart(app);
        });
        
        adapter.setOnItemClickListener(app -> {
            if (app.componentName != null) {
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(app.componentName.getPackageName());
                if (intent != null) {
                    getContext().startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        addView(recyclerView);

        letterIndexView = new LetterIndexView(getContext());
        letterIndexView.setVisibility(GONE);

        letterIndexView.setOnLetterSelectedListener(letter -> {
            scrollToLetter(letter);
        });
        letterIndexView.setOnClickListener(v -> {
                    //                letterIndexView.setVisibility(GONE)
                    letterIndexView.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .withEndAction(() -> letterIndexView.setVisibility(GONE))
                            .start();
                }
        );

        addView(letterIndexView);

        loadApps();
    }

    private void loadApps() {
        List<AppListItem> items = AppLoader.load(getContext());
        adapter.submit(items);
    }

    private void scrollToLetter(String letter) {
        List<AppListItem> items = adapter.getItems();

        for (int i = 0; i < items.size(); i++) {
            AppListItem item = items.get(i);
            if (item.type == AppListItem.TYPE_HEADER
                    && item.letter.equals(letter)) {

                recyclerView.scrollToPosition(i);
                return;
            }
        }
    }

    private void pinToStart(AppInfo app) {
        if (app == null || app.componentName == null) return;
        ComponentName cn = app.componentName;

        List<PinnedTile> tiles = TileStorage.load(getContext());

        for (PinnedTile t : tiles) {
            if (t.packageName.equals(cn.getPackageName())
                    && t.className.equals(cn.getClassName())) {
                return; // 已固定
            }
        }

        PinnedTile tile = new PinnedTile();
        tile.packageName = cn.getPackageName();
        tile.className = cn.getClassName();
        tile.label = app.name;

        tiles.add(tile);
        TileStorage.save(getContext(), tiles);
        TileChangeBus.notifyChanged();
    }


}
