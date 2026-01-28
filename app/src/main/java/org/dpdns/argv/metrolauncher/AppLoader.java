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

        appInfos.sort((a, b) ->
                a.name.compareToIgnoreCase(b.name));

        List<AppListItem> result = new ArrayList<>();

        String lastLetter = null;

        for (AppInfo app : appInfos) {
            String letter = app.name.substring(0, 1).toUpperCase();

            if (!letter.equals(lastLetter)) {
                lastLetter = letter;
                result.add(AppListItem.header(letter));
            }

            result.add(AppListItem.app(app));
        }

        return result;
    }
}
