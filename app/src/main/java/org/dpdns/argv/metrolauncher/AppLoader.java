package org.dpdns.argv.metrolauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.dpdns.argv.metrolauncher.model.AppInfo;
import org.dpdns.argv.metrolauncher.model.AppListItem;

import java.util.ArrayList;
import java.util.List;

public class AppLoader {

    public static List<AppListItem> load(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
        );

        List<AppInfo> appInfos = new ArrayList<>();

        for (ResolveInfo ri : apps) {
            // Hide own app icon (we inject Settings manually)
            if (ri.activityInfo.packageName.equals(context.getPackageName())) {
                continue;
            }

            AppInfo app = new AppInfo();
            app.name = ri.loadLabel(pm).toString();
            app.icon = ri.loadIcon(pm);
            app.packageName = ri.activityInfo.packageName;
            app.componentName = new android.content.ComponentName(
                    ri.activityInfo.packageName,
                    ri.activityInfo.name
            );
            appInfos.add(app);
        }

        // Inject Settings
        AppInfo settings = new AppInfo();
        String appName = context.getString(R.string.app_name);
        String settingsName = context.getString(R.string.settings);
        settings.name = appName + " " + settingsName;
        
        try {
            settings.icon = context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
             settings.icon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_settings);
        }
        
        settings.packageName = context.getPackageName();
        settings.componentName = new android.content.ComponentName(context, org.dpdns.argv.metrolauncher.activities.SettingsActivity.class);
        appInfos.add(settings);

        // Sort using Chinese Locale Collator (Handles Pinyin for Chinese, and standard A-Z for English)
        final java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.CHINA);
        // Use AlphabeticIndex for sectioning and primary sort key
        final android.icu.text.AlphabeticIndex.ImmutableIndex index = 
                new android.icu.text.AlphabeticIndex(java.util.Locale.CHINA).buildImmutableIndex();

        appInfos.sort((a, b) -> {
            // 1. Compare Alphabetic Buckets (Group by A, B, C...)
            int bucketA = index.getBucketIndex(a.name);
            int bucketB = index.getBucketIndex(b.name);
            
            if (bucketA != bucketB) {
                return Integer.compare(bucketA, bucketB);
            }
            
            // 2. Same Bucket: Prioritize English (Latin) over Chinese
            boolean isLatinA = isLatin(a.name);
            boolean isLatinB = isLatin(b.name);
            
            if (isLatinA && !isLatinB) return -1;
            if (!isLatinA && isLatinB) return 1;
            
            // 3. Same Type: Standard Collator Compare
            return collator.compare(a.name, b.name);
        });

        List<AppListItem> result = new ArrayList<>();

        String lastLetter = null;

        for (AppInfo app : appInfos) {
            int bucketIndex = index.getBucketIndex(app.name);
            String letter = index.getBucket(bucketIndex).getLabel();

            if (!letter.equals(lastLetter)) {
                lastLetter = letter;
                result.add(AppListItem.header(letter));
            }

            result.add(AppListItem.app(app));
        }

        return result;
    }
    
    private static boolean isLatin(String s) {
        if (s == null || s.isEmpty()) return false;
        char c = s.charAt(0);
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
