package org.dpdns.argv.metrolauncher.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.dpdns.argv.metrolauncher.UsageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultTilesProvider {

    public static List<PinnedTile> getDefaults(Context context) {
        List<PinnedTile> results = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // 1. Mandatory Default Tiles (Clock, Calendar, Photos)
        addSystemApp(context, results, addedPackages, new String[]{"clock", "alarm", "deskclock"});
        addSystemApp(context, results, addedPackages, new String[]{"calendar", "agenda"});
        addSystemApp(context, results, addedPackages, new String[]{"gallery", "photos", "image"});

        // 2. High Frequency Apps (Top 10)
        List<String> topApps = UsageHelper.getTopUsedApps(context, 10);
        for (String pkg : topApps) {
            addAppByPackage(context, results, addedPackages, pkg);
        }

        // 3. Fallback: If we have very few tiles (e.g. no permission), add some common apps if present
        if (results.size() < 4) {
             addAppByPackage(context, results, addedPackages, "com.android.settings");
             addAppByPackage(context, results, addedPackages, "com.android.chrome");
             addAppByPackage(context, results, addedPackages, "com.google.android.youtube");
        }

        return results;
    }

    private static void addSystemApp(Context context, List<PinnedTile> list, Set<String> added, String[] keywords) {
        // Simple heuristic: find an installed app with "clock", "calendar" etc in package name
        // This is not perfect but works for many stock ROMs.
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo info : apps) {
            String pkg = info.activityInfo.packageName.toLowerCase();
            for (String kw : keywords) {
                if (pkg.contains(kw)) {
                    addAppByPackage(context, list, added, info.activityInfo.packageName);
                    return; // Found one matching app for this category, stop
                }
            }
        }
    }

    private static void addAppByPackage(Context context, List<PinnedTile> list, Set<String> added, String pkgName) {
        if (added.contains(pkgName)) return;

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        if (intent != null && intent.getComponent() != null) {
            ComponentName cn = intent.getComponent();
            PinnedTile t = new PinnedTile();
            t.packageName = cn.getPackageName();
            t.className = cn.getClassName();
            
            try {
                CharSequence label = pm.getActivityInfo(cn, 0).loadLabel(pm);
                t.label = label.toString();
            } catch (PackageManager.NameNotFoundException e) {
                t.label = pkgName;
            }

            list.add(t);
            added.add(pkgName);
        }
    }
}
