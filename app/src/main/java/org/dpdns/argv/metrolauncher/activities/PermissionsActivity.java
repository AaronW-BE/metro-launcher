package org.dpdns.argv.metrolauncher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.dpdns.argv.metrolauncher.R;
import org.dpdns.argv.metrolauncher.util.LanguageManager;

public class PermissionsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permissions);

        final int basePadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.permissions_root), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                systemBars.left + basePadding, 
                systemBars.top + basePadding, 
                systemBars.right + basePadding, 
                systemBars.bottom + basePadding
            );
            return insets;
        });

        initPermissionUI();
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private TextView tvPermUsageStatus, tvPermCalendarStatus, tvPermMediaStatus;

    private void initPermissionUI() {
        tvPermUsageStatus = findViewById(R.id.tvPermUsageStatus);
        tvPermCalendarStatus = findViewById(R.id.tvPermCalendarStatus);
        tvPermMediaStatus = findViewById(R.id.tvPermMediaStatus);

        findViewById(R.id.btnPermUsage).setOnClickListener(v -> {
            startActivity(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });

        findViewById(R.id.btnPermCalendar).setOnClickListener(v -> {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CALENDAR}, 101);
            }
        });

        findViewById(R.id.btnPermMedia).setOnClickListener(v -> {
             java.util.List<String> needed = new java.util.ArrayList<>();
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                 if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    needed.add(android.Manifest.permission.READ_MEDIA_IMAGES);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                     if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) 
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        needed.add(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
                    }
                }
            } else {
                 if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    needed.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            if (!needed.isEmpty()) {
                androidx.core.app.ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 102);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void updatePermissionStatus() {
        // Usage
        boolean hasUsage = org.dpdns.argv.metrolauncher.UsageHelper.hasUsageStatsPermission(this);
        setPermStatus(tvPermUsageStatus, hasUsage);

        // Calendar
        boolean hasCalendar = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
        setPermStatus(tvPermCalendarStatus, hasCalendar);

        // Media
        boolean hasMedia = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hasMedia = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) 
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } else {
            hasMedia = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        setPermStatus(tvPermMediaStatus, hasMedia);
    }
    
    private void setPermStatus(TextView tv, boolean granted) {
        if (granted) {
            tv.setText(R.string.perm_status_granted);
            tv.setTextColor(android.graphics.Color.GREEN);
        } else {
            tv.setText(R.string.perm_status_denied);
            tv.setTextColor(0xFFE53935); // Red color
        }
    }
}
