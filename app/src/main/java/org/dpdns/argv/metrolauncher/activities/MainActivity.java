package org.dpdns.argv.metrolauncher.activities;

import android.os.Bundle;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.dpdns.argv.metrolauncher.R;

public class MainActivity extends AppCompatActivity {

    private OnBackPressedCallback metroBackCallback;

    private enum LauncherState {
        TILES,      // 磁贴主界面
        ALL_APPS,   // 应用列表（左滑后的界面）
        EDIT_MODE   // 磁贴编辑/排序模式
    }

    private LauncherState currentState = LauncherState.TILES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().setDecorFitsSystemWindows(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        metroBackCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackLogic();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, metroBackCallback);
        
        checkPermissions();
    }
    
    private void checkPermissions() {
        // 1. Check Usage Stats (for High-Frequency Apps)
        if (!org.dpdns.argv.metrolauncher.UsageHelper.hasUsageStatsPermission(this)) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("MetroLauncher needs 'Usage Access' usage to identify your most frequently used apps and display them on the start screen.")
                .setPositiveButton("Grant", (d, w) -> {
                    startActivity(new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
        }

        // 2. Check Runtime Permissions (for Live Tiles)
        java.util.List<String> needed = new java.util.ArrayList<>();
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            needed.add(android.Manifest.permission.READ_CALENDAR);
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                needed.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
             if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                needed.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        
        if (!needed.isEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 100);
        }
    }

    private void handleBackLogic() {
        switch (currentState) {
            case ALL_APPS:
                break;
            case EDIT_MODE:
                break;
            case TILES:
                break;
        }
    }

    private void updateUIState(LauncherState newState) {
        this.currentState = newState;

        // 动态判断：如果是主页，可能想让系统处理（比如显示预测性返回动画），
        // 或者完全由自己控制（true = 拦截，false = 交给系统）
        metroBackCallback.setEnabled(true);
    }

}