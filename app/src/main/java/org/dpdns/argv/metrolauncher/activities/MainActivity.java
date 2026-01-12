package org.dpdns.argv.metrolauncher.activities;

import android.view.WindowManager;
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

    private org.dpdns.argv.metrolauncher.ui.MetroRootLayout metroRootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
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
        
        metroRootLayout = findViewById(R.id.metroRoot);
        if (metroRootLayout != null) {
            metroRootLayout.setOnScrollListener(new org.dpdns.argv.metrolauncher.ui.MetroRootLayout.OnScrollListener() {
                @Override
                public void onScroll(float offset, float maxOffset) {
                    // offset is [0, -width], 0 is home, -width is app list.
                    // Parallax requires [0, 1] range usually.
                    // Let's map 0 -> 0.5 (center) and -width -> 0.0 or 1.0 depending on preference.
                    // Standard Android: 0 to 1. 0.5 is usually center.
                    
                    if (maxOffset == 0) return;
                    float progress = offset / maxOffset; // [0, 1]
                    
                    // Add a subtle range, e.g., 0.4 to 0.6
                    float wallpaperOffset = 0.4f + (progress * 0.2f);
                    
                    try {
                        android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(MainActivity.this);
                        wm.setWallpaperOffsets(metroRootLayout.getWindowToken(), wallpaperOffset, 0.5f);
                    } catch (Exception e) {
                        // ignore
                    }
                }

                @Override
                public void onStateChanged(int newState) {
                    currentState = newState == 0 ? LauncherState.TILES : LauncherState.ALL_APPS;
                    // handleBackLogic(); // Optional optimization
                }
            });
        }
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
            androidx.core.app.ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 100);
        }
    }

    private void handleBackLogic() {
        if (metroRootLayout == null) return;

        // 1. If in App List, go back to Home
        if (metroRootLayout.isShowingAppList()) {
            metroRootLayout.showHome();
            return;
        }

        // 2. If in Edit Mode on Home, exit Edit Mode
        if (metroRootLayout.getChildCount() > 0 && metroRootLayout.getChildAt(0) instanceof org.dpdns.argv.metrolauncher.ui.MetroHomeView) {
            org.dpdns.argv.metrolauncher.ui.MetroHomeView home = (org.dpdns.argv.metrolauncher.ui.MetroHomeView) metroRootLayout.getChildAt(0);
            if (home.isEditMode()) {
                home.setEditMode(false);
                return;
            }
        }
        
        // Otherwise, do nothing (Launcher usually stays on Home)
    }

    private void updateUIState(LauncherState newState) {
        this.currentState = newState;

        // 动态判断：如果是主页，可能想让系统处理（比如显示预测性返回动画），
        // 或者完全由自己控制（true = 拦截，false = 交给系统）
        metroBackCallback.setEnabled(true);
    }

}