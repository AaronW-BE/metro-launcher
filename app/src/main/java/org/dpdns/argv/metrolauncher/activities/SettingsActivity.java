package org.dpdns.argv.metrolauncher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.dpdns.argv.metrolauncher.R;
import org.dpdns.argv.metrolauncher.util.LanguageManager;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        
        final int basePadding = (int) android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                systemBars.left + basePadding, 
                systemBars.top + basePadding, 
                systemBars.right + basePadding, 
                systemBars.bottom + basePadding
            );
            return insets;
        });

        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        View btnLanguage = findViewById(R.id.btnLanguage);

        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        
        // Lock Layout Switch
        View containerLockLayout = findViewById(R.id.containerLockLayout);
        androidx.appcompat.widget.SwitchCompat switchLockLayout = findViewById(R.id.switchLockLayout);
        
        switchLockLayout.setChecked(org.dpdns.argv.metrolauncher.util.PreferenceManager.isLayoutLocked(this));
        
        containerLockLayout.setOnClickListener(v -> {
            boolean newState = !switchLockLayout.isChecked();
            switchLockLayout.setChecked(newState);
            org.dpdns.argv.metrolauncher.util.PreferenceManager.setLayoutLocked(this, newState);
        });
        switchLockLayout.setOnCheckedChangeListener((buttonView, isChecked) -> {
             org.dpdns.argv.metrolauncher.util.PreferenceManager.setLayoutLocked(this, isChecked);
        });

        updateLanguageText();
    }

    private void updateLanguageText() {
        String code = LanguageManager.getSavedLanguage(this);
        String label;
        switch (code) {
            case LanguageManager.LANG_EN:
                label = getString(R.string.lang_en);
                break;
            case LanguageManager.LANG_ZH:
                label = getString(R.string.lang_zh);
                break;
            case LanguageManager.LANG_HK:
                label = getString(R.string.lang_hk);
                break;
            default:
                label = getString(R.string.lang_auto);
                break;
        }
        tvCurrentLanguage.setText(label);
    }

    private void showLanguageDialog() {
        startActivity(new Intent(this, LanguageSettingsActivity.class));
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
