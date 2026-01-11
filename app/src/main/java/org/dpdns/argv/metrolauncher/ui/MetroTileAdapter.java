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

    public MetroTileAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void setTiles(List<TileItem> list) {
        tiles.clear();
        tiles.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return tiles.get(position).id;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TileView view = new TileView(context);

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
        holder.bind(item);

        MetroTileLayoutParams lp =
                (MetroTileLayoutParams) holder.itemView.getLayoutParams();

        lp.spanX = item.spanX;
        lp.spanY = item.spanY;
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }
}
