package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dpdns.argv.metrolauncher.model.TileItem;

public class TileView extends FrameLayout {

    private TextView titleView;

    public TileView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);

        titleView = new TextView(getContext());
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(16);
        titleView.setGravity(Gravity.BOTTOM | Gravity.START);
        titleView.setPadding(24, 24, 24, 24);

        addView(titleView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
    }

    public void bind(TileItem item) {
        setBackgroundColor(item.color);
        titleView.setText(item.title);
    }
}
