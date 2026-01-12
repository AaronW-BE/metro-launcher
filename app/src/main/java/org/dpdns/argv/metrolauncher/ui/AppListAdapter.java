package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.dpdns.argv.metrolauncher.model.AppInfo;
import org.dpdns.argv.metrolauncher.model.AppListItem;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnHeaderClickListener {
        void onHeaderClick();
    }

    public interface OnPinListener {
        void onPin(AppInfo app);
    }


    private OnHeaderClickListener headerClickListener;

    public void setOnHeaderClickListener(OnHeaderClickListener l) {
        headerClickListener = l;
    }

    private OnPinListener pinListener;

    public void setOnPinListener(OnPinListener l) {
        pinListener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(AppInfo app);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        itemClickListener = l;
    }

    private List<AppListItem> items = new ArrayList<>();

    public void submit(List<AppListItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == AppListItem.TYPE_HEADER) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextSize(24);
            tv.setPadding(32, 48, 32, 16);
            tv.setTextColor(Color.WHITE);
            return new HeaderVH(tv);
        } else {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(32, 24, 32, 24);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            ImageView icon = new ImageView(parent.getContext());
            icon.setLayoutParams(new LinearLayout.LayoutParams(96, 96));

            TextView name = new TextView(parent.getContext());
            name.setTextSize(18);
            name.setTextColor(Color.WHITE);
            name.setPadding(32, 0, 0, 0);

            layout.addView(icon);
            layout.addView(name);

            return new AppVH(layout, icon, name);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AppListItem item = items.get(position);

        if (holder instanceof HeaderVH) {
            HeaderVH vh = (HeaderVH) holder;
            vh.text.setText(item.letter);
            vh.text.setOnClickListener(v -> {
                if (headerClickListener != null) {
                    headerClickListener.onHeaderClick();
                }
            });

        }
        if (holder instanceof AppVH) {
            AppVH vh = (AppVH) holder;

            AppInfo app = item.app;
            vh.icon.setImageDrawable(app.icon);
            vh.name.setText(app.name);

            vh.itemView.setOnLongClickListener(v -> {
                showPinMenu(v.getContext(), app);
                return true;
            });

            vh.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(app);
                }
            });
        }
    }

    private void showPinMenu(Context c, AppInfo app) {
        new AlertDialog.Builder(c)
                .setTitle(app.name)
                .setItems(new String[]{"固定到开始屏幕"}, (d, i) -> {
                    if (i == 0 && pinListener != null) {
                        pinListener.onPin(app);
                    }
                })
                .show();
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView text;
        HeaderVH(View v) {
            super(v);
            text = (TextView) v;
        }
    }

    static class AppVH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        AppVH(View v, ImageView i, TextView n) {
            super(v);
            icon = i;
            name = n;
        }
    }

    public List<AppListItem> getItems() {
        return items;
    }
}
