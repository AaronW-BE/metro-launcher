package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.dpdns.argv.metrolauncher.model.TileItem;

import java.util.ArrayList;
import java.util.List;

public class MetroTileAdapter extends RecyclerView.Adapter<TileViewHolder> {

    private final Context context;
    private final List<TileItem> tiles = new ArrayList<>();
    private boolean isEditMode = false;

    public MetroTileAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void setTiles(List<TileItem> list) {
        tiles.clear();
        tiles.addAll(list);
        notifyDataSetChanged();
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return tiles.get(position).id;
    }

    @Override
    public int getItemViewType(int position) {
        TileItem item = tiles.get(position);
        String pkg = item.component.getPackageName().toLowerCase();
        if (pkg.contains("calendar")) return 1;
        if (pkg.contains("photo") || pkg.contains("gallery") || pkg.contains("image") || 
            pkg.contains("com.miui.gallery") || pkg.contains("com.sec.android.gallery3d") ||
            pkg.contains("google.android.apps.photos")) return 2;
        if (pkg.contains("clock") || pkg.contains("alarm")) return 3;
        return 0;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TileView view;
        if (viewType == 1) {
            view = new CalendarTileView(context);
        } else if (viewType == 2) {
            view = new PhotoTileView(context);
        } else if (viewType == 3) {
            view = new ClockTileView(context);
        } else {
            view = new TileView(context);
        }

        MetroTileLayoutParams lp = new MetroTileLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        view.setLayoutParams(lp);

        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = tiles.get(position);
        holder.bind(item, isEditMode);

        MetroTileLayoutParams lp =
                (MetroTileLayoutParams) holder.itemView.getLayoutParams();

        lp.spanX = item.spanX;
        lp.spanY = item.spanY;
    }

    public void moveItem(int from, int to) {
        TileItem item = tiles.remove(from);
        tiles.add(to, item);
        notifyItemMoved(from, to);
        saveToStorage();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < tiles.size()) {
            tiles.remove(position);
            notifyItemRemoved(position);
            saveToStorage();
        }
    }

    public void saveToStorage() {
        List<org.dpdns.argv.metrolauncher.model.PinnedTile> pinned = new ArrayList<>();
        for (TileItem item : tiles) {
            org.dpdns.argv.metrolauncher.model.PinnedTile t = new org.dpdns.argv.metrolauncher.model.PinnedTile();
            t.packageName = item.component.getPackageName();
            t.className = item.component.getClassName();
            t.label = item.title;
            t.spanX = item.spanX;
            t.spanY = item.spanY;
            pinned.add(t);
        }
        org.dpdns.argv.metrolauncher.model.TileStorage.save(context, pinned);
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }
}
