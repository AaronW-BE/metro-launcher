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
