package org.dpdns.argv.metrolauncher.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.dpdns.argv.metrolauncher.model.TileItem;

import java.util.Calendar;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class CalendarTileView extends LiveTileView {
    
    private TextView dayText;
    private TextView dateText;
    private TextView eventTitle;
    private TextView eventTime;
    private TextView eventLocation;

    public CalendarTileView(Context context) {
        super(context);
    }

    @Override
    protected View createFrontView() {
        // Layout: Day of week (Mon) + Big Date (12)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF00AFF0); // Metro Blue
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        dayText = new TextView(getContext());
        dayText.setTextColor(Color.WHITE);
        dayText.setTextSize(18);
        dayText.setGravity(Gravity.CENTER_HORIZONTAL);
        
        dateText = new TextView(getContext());
        dateText.setTextColor(Color.WHITE);
        dateText.setTextSize(48);
        dateText.setGravity(Gravity.CENTER_HORIZONTAL);
        dateText.setTypeface(null, android.graphics.Typeface.BOLD);
        
        layout.addView(dayText);
        layout.addView(dateText);
        
        updateDate();
        return layout;
    }

    @Override
    protected View createBackView() {
        // Layout: Event Title, Time, Location
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(0xFF00AFF0); // Metro Blue
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        eventTitle = new TextView(getContext());
        eventTitle.setTextColor(Color.WHITE);
        eventTitle.setTextSize(16);
        eventTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        
        eventTime = new TextView(getContext());
        eventTime.setTextColor(Color.LTGRAY);
        eventTime.setTextSize(14);
        
        eventLocation = new TextView(getContext());
        eventLocation.setTextColor(Color.LTGRAY);
        eventLocation.setTextSize(12);
        
        layout.addView(eventTitle);
        layout.addView(eventTime);
        layout.addView(eventLocation);
        
        updateEvents();
        return layout;
    }

    @Override
    public void bind(TileItem item, boolean isEditMode) {
        super.bind(item, isEditMode);
        initViews(); // Vital for Live Tiles
        if (frontView != null) frontView.setVisibility(VISIBLE);
        // Calendar tile is typically Metro Blue
        setBackgroundColor(0xFF00AFF0); 
        updateDate();
        updateEvents();
    }
    
    private void updateDate() {
         Calendar c = Calendar.getInstance();
         if (dayText != null) dayText.setText(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
         if (dateText != null) dateText.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
    }
    
    private void updateEvents() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) 
                != PackageManager.PERMISSION_GRANTED) {
             if (eventTitle != null) eventTitle.setText("No permissions");
             return;
        }
        
        // Simple query for next event
        new Thread(() -> {
            try {
                long now = System.currentTimeMillis();
                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, now);
                ContentUris.appendId(builder, now + 86400000 * 7); // Next 7 days

                Cursor cursor = getContext().getContentResolver().query(
                        builder.build(),
                        new String[]{CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_LOCATION},
                        null, null, CalendarContract.Instances.BEGIN + " ASC LIMIT 1");
                
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String title = cursor.getString(0);
                        long begin = cursor.getLong(1);
                        String loc = cursor.getString(2);
                        
                        String timeStr = DateFormat.format("EEE, HH:mm", begin).toString();
                        
                        post(() -> {
                            if (eventTitle != null) eventTitle.setText(title);
                            if (eventTime != null) eventTime.setText(timeStr);
                            if (eventLocation != null) eventLocation.setText(loc);
                        });
                    } else {
                        post(() -> {
                           if (eventTitle != null) eventTitle.setText("No events");
                        });
                    }
                    cursor.close();
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
