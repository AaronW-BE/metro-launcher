package org.dpdns.argv.metrolauncher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.dpdns.argv.metrolauncher.R;
import org.dpdns.argv.metrolauncher.util.LanguageManager;

public class LanguageSettingsActivity extends AppCompatActivity {

    private LinearLayout listContainer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_language_settings);

        final int basePadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.language_settings_root), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                systemBars.left + basePadding, 
                systemBars.top + basePadding, 
                systemBars.right + basePadding, 
                systemBars.bottom + basePadding
            );
            return insets;
        });

        listContainer = findViewById(R.id.language_list_container);
        setupLanguageList();
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupLanguageList() {
        String currentCode = LanguageManager.getSavedLanguage(this);

        addLanguageItem(getString(R.string.lang_auto), LanguageManager.LANG_AUTO, currentCode);
        addLanguageItem(getString(R.string.lang_en), LanguageManager.LANG_EN, currentCode);
        addLanguageItem(getString(R.string.lang_zh), LanguageManager.LANG_ZH, currentCode);
        addLanguageItem(getString(R.string.lang_hk), LanguageManager.LANG_HK, currentCode);
    }

    private void addLanguageItem(String label, String code, String currentCode) {
        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24); // Larger text for list items
        textView.setPadding(0, 24, 0, 24); // Vertical padding
        
        if (code.equals(currentCode)) {
            // Highlight selected language (Metro style often uses accent color, but for now we'll use white vs gray or bold)
            textView.setTextColor(Color.WHITE); 
            // Could add an accent mark or prefix here if desired
        } else {
            textView.setTextColor(Color.LTGRAY);
        }

        textView.setOnClickListener(v -> {
            if (!code.equals(currentCode)) {
                LanguageManager.setLanguage(this, code);
                restartApp();
            }
        });

        // Add ripple effect
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        textView.setBackgroundResource(outValue.resourceId);
        textView.setClickable(true);
        textView.setFocusable(true);

        listContainer.addView(textView);
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
