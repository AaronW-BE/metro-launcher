package org.dpdns.argv.metrolauncher.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.dpdns.argv.metrolauncher.model.TileItem;

public class ClockTileView extends LiveTileView {

    private TextView timeText;
    private TextView amPmText;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.getDefault());

    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    };

    public ClockTileView(Context context) {
        super(context);
    }

    @Override
    protected View createFrontView() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        timeText = new TextView(getContext());
        timeText.setTextColor(Color.WHITE);
        timeText.setTextSize(36);
        timeText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        timeText.setGravity(Gravity.CENTER);

        amPmText = new TextView(getContext());
        amPmText.setTextColor(Color.WHITE);
        amPmText.setTextSize(14);
        amPmText.setGravity(Gravity.CENTER);

        layout.addView(timeText);
        layout.addView(amPmText);

        updateTime();
        return layout;
    }

    @Override
    protected View createBackView() {
        // Clock tile in Metro often doesn't flip, or flips to day/date
        // Let's show Day & Date on the back
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        TextView dayText = new TextView(getContext());
        dayText.setTextColor(Color.WHITE);
        dayText.setTextSize(18);
        dayText.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date()));

        TextView dateText = new TextView(getContext());
        dateText.setTextColor(Color.WHITE);
        dateText.setTextSize(24);
        dateText.setText(new SimpleDateFormat("MMMM d", Locale.getDefault()).format(new Date()));

        layout.addView(dayText);
        layout.addView(dateText);

        return layout;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getContext().unregisterReceiver(timeReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public void bind(TileItem item, boolean isEditMode) {
        super.bind(item, isEditMode);
        initViews();
        
        // Hide default icon and title as we render our own clock face
        if (iconView != null) iconView.setVisibility(GONE);
        if (titleView != null) titleView.setVisibility(GONE);
        
        // Ensure binding updates immediately
        updateTime();
    }
    
    private void updateTime() {
        if (timeText != null) {
            Date now = new Date();
            timeText.setText(timeFormat.format(now));
            amPmText.setText(amPmFormat.format(now));
        }
    }
}
