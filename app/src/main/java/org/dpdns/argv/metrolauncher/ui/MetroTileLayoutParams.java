package org.dpdns.argv.metrolauncher.ui;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class MetroTileLayoutParams extends RecyclerView.LayoutParams {
    public int spanX = 1;
    public int spanY = 1;

    public MetroTileLayoutParams(int width, int height) {
        super(width, height);
    }

    public MetroTileLayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }
}
