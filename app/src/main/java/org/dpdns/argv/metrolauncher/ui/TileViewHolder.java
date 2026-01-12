package org.dpdns.argv.metrolauncher.ui;

import androidx.recyclerview.widget.RecyclerView;

import org.dpdns.argv.metrolauncher.model.TileItem;

public class TileViewHolder extends RecyclerView.ViewHolder {

    public TileViewHolder(TileView itemView) {
        super(itemView);
    }

    public void bind(TileItem item, boolean isEditMode) {
        ((TileView) itemView).bind(item, isEditMode);
    }
}
